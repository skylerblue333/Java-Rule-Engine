from fastapi.testclient import TestClient
from src.main import app

client = TestClient(app)

def test_health():
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json()["status"] == "healthy"

def test_publish_event():
    r = client.post("/api/v1/events", json={"event_type": "user.created", "payload": {"id": 1}, "source": "auth-service"})
    assert r.status_code == 200
    assert r.json()["status"] == "published"

def test_list_events():
    r = client.get("/api/v1/events")
    assert r.status_code == 200
    assert "events" in r.json()

