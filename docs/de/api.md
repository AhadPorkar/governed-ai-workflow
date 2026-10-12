# HTTP-API und Verträge

Die maschinenlesbare OpenAPI-3.1-Datei liegt im Quellpaket unter `openapi/governance.openapi.json`, nach Overlay-Anwendung unter `docs/governed-ai/governance.openapi.json`.

Lokale Basis: `http://127.0.0.1:8095/api`. Spring-Basis: `http://localhost:8081/governance/api`. JSON-Anfragen sind auf 16 KiB begrenzt. Query-Parameter werden nicht unterstützt. Authentifizierte Identitäten erhalten pro Prozess höchstens 120 Anfragen pro Minute; das ist weder ein verteilter noch ein anonymer Perimeterschutz.

| Methode und relativer Pfad | Rolle | Vertrag |
|---|---|---|
| `GET /health` | Öffentlich | Version, Modus, Planer; keine vollständige Abhängigkeitsbereitschaft. |
| `GET /session` | Authentifiziert | Serverseitige Identität, Mandant, Rollen und Planer. |
| `GET /dashboard` | READ | Eigene Bestellungen/Fälle, Regeln und Audit-Gültigkeit. |
| `GET /cases/{id}` | READ | Fall, Nachweise, Audit und gegebenenfalls Beleg. |
| `POST /cases` | OPERATOR | Exakt `orderId`, `request`; `Idempotency-Key` ist Pflicht. |
| `POST /cases/{id}/plan` | OPERATOR oder WORKER | Leeres JSON-Objekt; dauerhafte Planungs-Lease. |
| `POST /cases/{id}/decisions` | REVIEWER | Exakt `proposalDigest`, `approve`, `reason`. |
| `GET /jobs` | WORKER | Bereite Jobs des eigenen Mandanten. |
| `POST /jobs/{id}/run` | WORKER | Leeres Objekt; gespeicherte Freigabe wird erneut geprüft. |
| `POST /jobs/{id}/ack` | WORKER | Exakt `receiptId`, passend zum gespeicherten Beleg. |

`Authorization: Bearer <token>` verwenden; POST verlangt `Content-Type: application/json`. Browserdaten dürfen weder Rolle noch Mandant vorgeben. Zusätzliche Identitäts-, Aktions- oder Freigabefelder werden abgelehnt und nicht stillschweigend ignoriert.

## Fall erstellen

```http
POST /api/cases
Authorization: Bearer YOUR_OPERATOR_TOKEN
Idempotency-Key: example-order-1001-01
Content-Type: application/json

{"orderId":"ORD-1001","request":"Bitte erstatten Sie meine Zahlung."}
```

Erfolg liefert `201`. Derselbe Schlüssel mit identischem Inhalt für dieselbe Identität und denselben Mandanten liefert den bestehenden Fall. Abweichender Inhalt liefert `409 IDEMPOTENCY_CONFLICT`. Das Schlüsselmuster lautet `[A-Za-z0-9][A-Za-z0-9._-]{0,99}`. Die Anfrage muss nichtleer sein und darf maximal 4.000 Java-String-Zeichen enthalten.

Die Planung erhält `{}`. Ein zulässiger Vorschlag ist danach `PENDING_APPROVAL`, nicht genehmigt. Geldbeträge sind ganzzahlige Eurocent.

```json
{
  "action": "REFUND",
  "amountCents": 12900,
  "supplierId": "",
  "explanation": "Vorschlag zur Erstattung, vorbehaltlich der unabhaengigen Pruefung.",
  "citations": ["POLICY-ORDER-EXCEPTION@2026-10-12.1"]
}
```

## Freigabe ohne Aktionsänderung

```json
{
  "proposalDigest": "COPY_THE_EXACT_64_CHARACTER_DIGEST_FROM_THE_CASE",
  "approve": true,
  "reason": "Kundenanfrage, gebuchten Betrag und genaue Aktion geprueft."
}
```

Den Hash exakt aus dem Fall übernehmen und nicht im Client neu berechnen. Die Entscheidung akzeptiert keinen Ersatzbetrag. Ein geänderter Vorschlag braucht eine neue Revision und Freigabe. Zustimmung und Ablehnung benötigen eine Begründung mit maximal 600 Zeichen.

Die Freigabefrist endet 15 Minuten nach Erstellung des Vorschlags, nicht nach dem Klick der prüfenden Person. Ein verspäteter Ausführungsversuch scheitert auch dann, wenn ein Timer den sichtbaren Zustand noch nicht aktualisiert hat.

## Fehler und Wiederholungen

Die Antwort enthält stabile Felder `type`, `title`, `status`. Typische Statuscodes: `401` bei fehlenden/abgelaufenen Zugangsdaten, `403` bei Rolle/Selbstfreigabe, verborgenes `404` bei fremdem Mandanten, `409` bei veraltetem Zustand oder Hash, `413` bei zu großer Anfrage, `415` beim falschen Inhaltstyp und `429` beim Ratenlimit. Transportnahe Fehlerbezeichnungen können zwischen JDK und Spring abweichen.

Wiederholte Ausführung liefert den vorhandenen Beleg und erzeugt ein Wiederholungsereignis im Audit. Eine wiederholte menschliche Entscheidung ist dagegen ein Zustandskonflikt. Jobbestätigung und Geschäftseffekt bleiben bewusst getrennt.


---
[Überblick](overview.md) | [Schnellstart](quickstart.md) | [Architektur](architecture.md) | [Integration](integration.md) | [API](api.md) | [Sicherheit](security.md) | [Betrieb](operations.md) | [Tests](testing.md) | [Evaluation](evaluation.md) | [Release](release.md) | [Demo](demo.md) | [Entscheidungen](decisions.md) | [Quellen](sources.md)
