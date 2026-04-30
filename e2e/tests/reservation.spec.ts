import { test, expect } from '@playwright/test';
import { ReservationClient } from '../clients/ReservationClient';
import { SetupClient } from '../clients/SetupClient';
import { reservationData, reservationUpdateData } from '../data/testData';

let guestId: string;
let roomId: string;
let reservationId: string;

test.beforeAll(async ({ request }) => {
  const setup = new SetupClient(request);
  guestId = await setup.createGuest();
  roomId = await setup.createRoom();

  const client = new ReservationClient(request);
  const response = await client.create(reservationData(guestId, roomId));
  const body = await response.json();
  reservationId = body.id;
});

// AC1: POST /reservation — 200 OK with ID
test('AC1 - create a reservation returns 200 and an ID', async () => {
  expect(reservationId).toBeTruthy();
});

// AC3: GET /reservation/{id} — 200 OK with correct data
test('AC3 - get reservation by ID returns 200 and correct data', async ({ request }) => {
  const client = new ReservationClient(request);

  const response = await client.getById(reservationId);

  expect(response.status()).toBe(200);
  const body = await response.json();
  expect(body.id).toBe(reservationId);
  expect(body.guestId).toBe(guestId);
  expect(body.roomId).toBe(roomId);
});

// AC4: PUT /reservation/{id} — 200 OK with updated data
test('AC4 - update a reservation returns 200 and updated data', async ({ request }) => {
  const client = new ReservationClient(request);

  const response = await client.update(reservationId, reservationUpdateData(guestId, roomId));

  expect(response.status()).toBe(200);
  const body = await response.json();
  expect(body.guestsCount).toBe(3);
  expect(body.notes).toBe('ATDD updated reservation');
});

// AC5: DELETE /reservation/{id} — 200 OK, reservation deleted
test('AC5 - delete a reservation returns 200 and it no longer exists', async ({ request }) => {
  const client = new ReservationClient(request);

  const createResponse = await client.create(reservationData(guestId, roomId));
  const { id } = await createResponse.json();

  const deleteResponse = await client.delete(id);
  expect(deleteResponse.status()).toBe(200);

  const getResponse = await client.getById(id);
  expect(getResponse.status()).toBe(404);
});

// AC6: GET /reservation/{id} non-existent — 404
test('AC6 - get reservation with non-existent ID returns 404', async ({ request }) => {
  const client = new ReservationClient(request);
  const fakeId = '00000000-0000-0000-0000-000000000000';

  const response = await client.getById(fakeId);

  expect(response.status()).toBe(404);
});

// AC2: GET /reservation — 200 OK with list
test('AC2 - get all reservations returns 200 and a list', async ({ request }) => {
  const client = new ReservationClient(request);

  const response = await client.getAll();

  expect(response.status()).toBe(200);
  const body = await response.json();
  expect(Array.isArray(body)).toBe(true);
  expect(body.length).toBeGreaterThan(0);
});
