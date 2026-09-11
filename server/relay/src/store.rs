use serde::{Deserialize, Serialize};
use std::collections::HashMap;
use std::sync::Arc;
use tokio::sync::RwLock;
use uuid::Uuid;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct KeyBundleRecord {
    pub identity_id: String,
    pub device_id: String,
    pub identity_key: String,
    pub signed_pre_key: String,
    pub signed_pre_key_signature: String,
    pub one_time_pre_key: Option<String>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct DeviceRecord {
    pub device_id: String,
    pub identity_id: String,
    pub push_token_hash: Option<String>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct MessageRecord {
    pub id: String,
    pub recipient_device_id: String,
    pub sender_identity_id: String,
    pub conversation_id: String,
    pub ciphertext_b64: String,
    pub sent_at_epoch_ms: i64,
}

/// In-memory store for local development. Replace with Redis in Phase 5.
#[derive(Clone, Default)]
pub struct InMemoryStore {
    inner: Arc<RwLock<StoreData>>,
}

#[derive(Default)]
struct StoreData {
    devices: HashMap<String, DeviceRecord>,
    keys: HashMap<(String, String), KeyBundleRecord>,
    queues: HashMap<String, Vec<MessageRecord>>,
}

impl InMemoryStore {
    pub fn new() -> Self {
        Self::default()
    }

    pub async fn register_device(&self, record: DeviceRecord) {
        let mut data = self.inner.write().await;
        data.devices.insert(record.device_id.clone(), record);
    }

    pub async fn unregister_device(&self, device_id: &str) {
        let mut data = self.inner.write().await;
        data.devices.remove(device_id);
        data.queues.remove(device_id);
        data.keys.retain(|(_, d), _| d != device_id);
    }

    pub async fn put_key_bundle(&self, bundle: KeyBundleRecord) {
        let mut data = self.inner.write().await;
        data.keys.insert(
            (bundle.identity_id.clone(), bundle.device_id.clone()),
            bundle,
        );
    }

    pub async fn get_key_bundle(
        &self,
        identity_id: &str,
        device_id: &str,
    ) -> Option<KeyBundleRecord> {
        let data = self.inner.read().await;
        data.keys
            .get(&(identity_id.to_string(), device_id.to_string()))
            .cloned()
    }

    pub async fn enqueue_message(&self, message: MessageRecord) {
        let mut data = self.inner.write().await;
        data.queues
            .entry(message.recipient_device_id.clone())
            .or_default()
            .push(message);
    }

    pub async fn fetch_messages(&self, device_id: &str) -> Vec<MessageRecord> {
        let data = self.inner.read().await;
        data.queues.get(device_id).cloned().unwrap_or_default()
    }

    pub async fn ack_message(&self, device_id: &str, message_id: &str) -> bool {
        let mut data = self.inner.write().await;
        if let Some(queue) = data.queues.get_mut(device_id) {
            let before = queue.len();
            queue.retain(|m| m.id != message_id);
            return queue.len() < before;
        }
        false
    }

    pub fn generate_message_id() -> String {
        Uuid::new_v4().to_string()
    }
}
