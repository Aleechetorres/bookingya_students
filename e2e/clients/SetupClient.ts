import { APIRequestContext } from '@playwright/test';
import { guestData, roomData } from '../data/testData';

export class SetupClient {
  constructor(private readonly request: APIRequestContext) {}

  async createGuest(): Promise<string> {
    const response = await this.request.post('guest', { data: guestData() });
    const body = await response.json();
    return body.id;
  }

  async createRoom(): Promise<string> {
    const response = await this.request.post('room', { data: roomData() });
    const body = await response.json();
    return body.id;
  }
}
