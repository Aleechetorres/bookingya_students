import { APIRequestContext, APIResponse } from '@playwright/test';

export class ReservationClient {
  private readonly endpoint = 'reservation';

  constructor(private readonly request: APIRequestContext) {}

  async create(data: object): Promise<APIResponse> {
    return this.request.post(this.endpoint, { data });
  }

  async getAll(): Promise<APIResponse> {
    return this.request.get(this.endpoint);
  }

  async getById(id: string): Promise<APIResponse> {
    return this.request.get(`${this.endpoint}/${id}`);
  }

  async update(id: string, data: object): Promise<APIResponse> {
    return this.request.put(`${this.endpoint}/${id}`, { data });
  }

  async delete(id: string): Promise<APIResponse> {
    return this.request.delete(`${this.endpoint}/${id}`);
  }
}
