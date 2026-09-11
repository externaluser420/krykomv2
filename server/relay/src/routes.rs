use crate::error::RelayError;
use crate::store::{DeviceRecord, InMemoryStore, KeyBundleRecord, MessageRecord};
use crate::AppState;
use axum::extract::{Path, State};
use axum::routing::{get, post, put};
use axum::{Json, Router};
use serde::{Deserialize, Serialize};

pub fn api_routes() -> Router<AppState> {
    Router::new()
        .route("/health", get(health))
        .route(
            "/v1/devices/:device_id",
            put(register_device).delete(unregister_device),
        )
        .route(
            "/v1/accounts/:identity_id/devices/:device_id/keys",
            put(put_keys).get(get_keys),
        )
        .route("/v1/messages", post(send_message))
        .route("/v1/messages/pending/:device_id", get(fetch_pending))
        .route("/v1/messages/:message_id/ack", post(ack_message))
}

async fn health() -> Json<HealthResponse> {
    Json(HealthResponse {
        status: "ok",
        version: env!("CARGO_PKG_VERSION"),
        storage: "memory",
    })
}

#[derive(Serialize)]
struct HealthResponse {
    status: &'static str,
    version: &'static str,
    storage: &'static str,
}

#[derive(Deserialize)]
struct RegisterDeviceRequest {
    identity_id: String,
    push_token_hash: Option<String>,
}

async fn register_device(
    State(state): State<AppState>,
    Path(device_id): Path<String>,
    Json(body): Json<RegisterDeviceRequest>,
) -> Result<Json<DeviceRecord>, RelayError> {
    let record = DeviceRecord {
        device_id: device_id.clone(),
        identity_id: body.identity_id,
        push_token_hash: body.push_token_hash,
    };
    state.store.register_device(record.clone()).await;
    Ok(Json(record))
}

async fn unregister_device(
    State(state): State<AppState>,
    Path(device_id): Path<String>,
) -> Result<Json<StatusJson>, RelayError> {
    state.store.unregister_device(&device_id).await;
    Ok(Json(StatusJson { ok: true }))
}

async fn put_keys(
    State(state): State<AppState>,
    Path((identity_id, device_id)): Path<(String, String)>,
    Json(bundle): Json<KeyBundleRecord>,
) -> Result<Json<KeyBundleRecord>, RelayError> {
    if bundle.identity_id != identity_id || bundle.device_id != device_id {
        return Err(RelayError::BadRequest("identity/device mismatch".into()));
    }
    state.store.put_key_bundle(bundle.clone()).await;
    Ok(Json(bundle))
}

async fn get_keys(
    State(state): State<AppState>,
    Path((identity_id, device_id)): Path<(String, String)>,
) -> Result<Json<KeyBundleRecord>, RelayError> {
    state
        .store
        .get_key_bundle(&identity_id, &device_id)
        .await
        .map(Json)
        .ok_or(RelayError::NotFound)
}

#[derive(Deserialize)]
struct SendMessageRequest {
    recipient_device_id: String,
    sender_identity_id: String,
    conversation_id: String,
    ciphertext_b64: String,
    sent_at_epoch_ms: i64,
}

#[derive(Serialize)]
struct SendMessageResponse {
    id: String,
}

async fn send_message(
    State(state): State<AppState>,
    Json(body): Json<SendMessageRequest>,
) -> Result<Json<SendMessageResponse>, RelayError> {
    let id = InMemoryStore::generate_message_id();
    let record = MessageRecord {
        id: id.clone(),
        recipient_device_id: body.recipient_device_id,
        sender_identity_id: body.sender_identity_id,
        conversation_id: body.conversation_id,
        ciphertext_b64: body.ciphertext_b64,
        sent_at_epoch_ms: body.sent_at_epoch_ms,
    };
    state.store.enqueue_message(record).await;
    Ok(Json(SendMessageResponse { id }))
}

async fn fetch_pending(
    State(state): State<AppState>,
    Path(device_id): Path<String>,
) -> Json<Vec<MessageRecord>> {
    Json(state.store.fetch_messages(&device_id).await)
}

#[derive(Deserialize)]
struct AckRequest {
    device_id: String,
}

#[derive(Serialize)]
struct StatusJson {
    ok: bool,
}

async fn ack_message(
    State(state): State<AppState>,
    Path(message_id): Path<String>,
    Json(body): Json<AckRequest>,
) -> Result<Json<StatusJson>, RelayError> {
    let removed = state.store.ack_message(&body.device_id, &message_id).await;
    if removed {
        Ok(Json(StatusJson { ok: true }))
    } else {
        Err(RelayError::NotFound)
    }
}
