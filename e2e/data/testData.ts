export const guestData = () => ({
  identification: `TEST-${Date.now()}`,
  name: 'Test Guest',
  email: `guest${Date.now()}@test.com`,
});

export const roomData = () => ({
  code: `R-${Date.now()}`,
  name: 'Test Room',
  city: 'Medellín',
  maxGuests: 4,
  nightlyPrice: 150.00,
  available: true,
});

export const reservationData = (guestId: string, roomId: string) => ({
  guestId,
  roomId,
  checkIn: '2026-06-01T14:00:00',
  checkOut: '2026-06-05T12:00:00',
  guestsCount: 2,
  notes: 'ATDD test reservation',
});

export const reservationUpdateData = (guestId: string, roomId: string) => ({
  guestId,
  roomId,
  checkIn: '2026-07-10T14:00:00',
  checkOut: '2026-07-15T12:00:00',
  guestsCount: 3,
  notes: 'ATDD updated reservation',
});
