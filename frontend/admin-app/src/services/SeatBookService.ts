import axios from 'axios';

const BASE_URL = 'http://localhost:8000/api/admin/seats'; // Adjusted to match TripService style

export interface SeatBooking {
  seatNo: string;
  bookingStatus: 'booked' | 'unbooked';
  studentId: string | null;
  studentMail: string | null;
  bookingDate: Date | null;
  bookingTime: string | null;
}

export interface SeatUpdateData {
  bookingStatus: 'booked' | 'unbooked';
  studentId?: string;
  studentMail?: string;
}

export class SeatBookService {
  static async initializeSeats(tripId: string) {
    try {
      const response = await axios.post(`${BASE_URL}/create/${tripId}`);
      return response.data;
    } catch (error) {
      throw new Error('Failed to initialize seats');
    }
  }

  static async getAllSeats(tripId: string) {
    try {
      const response = await axios.get(`${BASE_URL}/${tripId}`);
      return response.data;
    } catch (error) {
      throw new Error('Failed to fetch seats');
    }
  }

  static async getBookedSeats(tripId: string) {
    try {
      const response = await axios.get(`${BASE_URL}/${tripId}/booked`);
      return response.data;
    } catch (error) {
      throw new Error('Failed to fetch booked seats');
    }
  }

  static async getStudentBookings(tripId: string, studentId: string) {
    try {
      const response = await axios.get(`${BASE_URL}/${tripId}/student/${studentId}`);
      return response.data;
    } catch (error) {
      throw new Error('Failed to fetch student bookings');
    }
  }

  static async getSeatByNumber(tripId: string, seatNo: string) {
    try {
      const response = await axios.get(`${BASE_URL}/${tripId}/${seatNo}`);
      return response.data;
    } catch (error) {
      throw new Error('Failed to fetch seat');
    }
  }

  static async updateSeat(tripId: string, seatNo: string, updateData: SeatUpdateData) {
    try {
      const response = await axios.put(`${BASE_URL}/${tripId}/${seatNo}`, updateData);
      return response.data;
    } catch (error) {
      throw new Error('Failed to update seat');
    }
  }

  static async updateMultipleSeats(tripId: string, seats: Array<{ seatNo: string } & SeatUpdateData>) {
    try {
      const response = await axios.put(`${BASE_URL}/${tripId}`, { seats });
      return response.data;
    } catch (error) {
      throw new Error('Failed to update seats');
    }
  }
}

export default SeatBookService;