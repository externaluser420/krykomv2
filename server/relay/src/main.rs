//! Veil relay server — stateless encrypted message relay (ADR-001).
//! Redis-backed storage wired in Phase 5; in-memory store for local dev.

mod config;
mod error;
mod routes;
mod store;

use axum::Router;
use std::net::SocketAddr;
use store::InMemoryStore;
use tower_http::cors::CorsLayer;
use tower_http::trace::TraceLayer;
use tracing_subscriber::{layer::SubscriberExt, util::SubscriberInitExt};

#[derive(Clone)]
pub struct AppState {
    pub store: InMemoryStore,
}

#[tokio::main]
async fn main() {
    tracing_subscriber::registry()
        .with(
            tracing_subscriber::EnvFilter::try_from_default_env()
                .unwrap_or_else(|_| "veil_relay=debug,tower_http=debug".into()),
        )
        .with(tracing_subscriber::fmt::layer())
        .init();

    let config = config::RelayConfig::from_env();
    let _redis = config.redis_url.as_ref(); // Phase 5: Redis-backed store
    let state = AppState {
        store: InMemoryStore::new(),
    };

    let app = Router::new()
        .merge(routes::api_routes())
        .layer(CorsLayer::permissive())
        .layer(TraceLayer::new_for_http())
        .with_state(state);

    let addr = SocketAddr::from(([0, 0, 0, 0], config.port));
    tracing::info!("veil-relay listening on {addr} (storage=memory, redis=disabled)");
    let listener = tokio::net::TcpListener::bind(addr)
        .await
        .expect("bind failed");
    axum::serve(listener, app).await.expect("server failed");
}
