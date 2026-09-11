#[allow(dead_code)]
pub struct RelayConfig {
    pub port: u16,
    pub redis_url: Option<String>,
}

impl RelayConfig {
    pub fn from_env() -> Self {
        Self {
            port: std::env::var("VEIL_RELAY_PORT")
                .ok()
                .and_then(|p| p.parse().ok())
                .unwrap_or(8080),
            redis_url: std::env::var("VEIL_REDIS_URL").ok(),
        }
    }
}
