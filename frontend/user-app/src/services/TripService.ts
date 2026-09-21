import axios from 'axios';

const BASE_URL = 'http://localhost:8000/api/admin'; // Adjust this to match your backend URL

export interface TripData {
  _id?: string;
  busID: string;
  tripID: string;
  startlocation: string;
  destination: string;
  date: string;
  departuretime: string;
}

export class TripService {
  static async createTrip(tripData: TripData) {
    try {
      const response = await axios.post(`${BASE_URL}/create-trip`, tripData);
      return response.data;
    } catch (error) {
      throw new Error('Failed to create trip');
    }
  }

  static async getAllTrips() {
    try {
      const response = await axios.get(`${BASE_URL}/get-trip`);
      return response.data;
    } catch (error) {
      throw new Error('Failed to fetch trips');
    }
  }

  static async getTripById(id: string) {
    try {
      const response = await axios.get(`${BASE_URL}/get-trip/${id}`);
      return response.data;
    } catch (error) {
      throw new Error('Failed to fetch trip');
    }
  }

  static async getTripsByBusId(busId: string) {
    try {
      const response = await axios.get(`${BASE_URL}/get-trip/${busId}`);
      return response.data;
    } catch (error) {
      throw new Error('Failed to fetch trips for bus');
    }
  }

  static async updateTrip(id: string, tripData: TripData) {
    try {
      const response = await axios.put(`${BASE_URL}/update-trip/${id}`, tripData);
      return response.data;
    } catch (error) {
      throw new Error('Failed to update trip');
    }
  }

  static async deleteTrip(id: string) {
    try {
      const response = await axios.delete(`${BASE_URL}/delete-trip/${id}`);
      return response.data;
    } catch (error) {
      throw new Error('Failed to delete trip');
    }
  }
}