from fastapi.testclient import TestClient

from app.main import app


client = TestClient(app)


def test_should_return_health_ok() -> None:
    response = client.get("/health")

    assert response.status_code == 200
    assert response.json() == {"status": "ok"}


def test_should_summarize_exception() -> None:
    payload = {
        "shipment_id": "SHIP-101",
        "exception_type": "DELAYED",
        "latest_status": "IN_TRANSIT",
        "operator_notes": "Weather issue",
    }

    response = client.post("/api/assistant/summarize", json=payload)

    assert response.status_code == 200
    body = response.json()
    assert "SHIP-101" in body["summary"]
    assert "carrier" in body["recommended_next_action"].lower()
