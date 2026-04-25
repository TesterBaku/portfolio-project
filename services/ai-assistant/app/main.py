from fastapi import FastAPI
from pydantic import BaseModel, Field


class ShipmentExceptionRequest(BaseModel):
    shipment_id: str = Field(min_length=1)
    exception_type: str = Field(min_length=1)
    latest_status: str = Field(min_length=1)
    operator_notes: str = ""


class SummaryResponse(BaseModel):
    summary: str
    recommended_next_action: str


app = FastAPI(title="AI Assistant Service", version="0.1.0")


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}


@app.post("/api/assistant/summarize", response_model=SummaryResponse)
def summarize_exception(payload: ShipmentExceptionRequest) -> SummaryResponse:
    summary = (
        f"Shipment {payload.shipment_id} is currently '{payload.latest_status}' and reported "
        f"with exception '{payload.exception_type}'."
    )

    recommended = "Contact carrier for updated ETA and notify the customer with the new timeline."
    if payload.operator_notes:
        recommended = (
            "Review operator notes, confirm delay cause with the carrier, then notify customer and update "
            "shipment status in control tower."
        )

    return SummaryResponse(summary=summary, recommended_next_action=recommended)
