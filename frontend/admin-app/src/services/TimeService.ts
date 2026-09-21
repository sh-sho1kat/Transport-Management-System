// src/services/TimeService.ts
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8000/api/admin'; // Adjust the port according to your backend

export interface TimeEntry {
  _id?: string;
  time: string;
}

export const TimeService = {
  // Create new time entry
  createTime: async (time: string): Promise<TimeEntry> => {
    try {
      const response = await axios.post(`${API_BASE_URL}/create-time`, { time });
      return response.data.newTime;
    } catch (error) {
      throw new Error('Failed to create time entry');
    }
  },

  // Get all time entries
  getAllTimes: async (): Promise<TimeEntry[]> => {
    try {
      const response = await axios.get(`${API_BASE_URL}/get-time`);
      return response.data;
    } catch (error) {
      throw new Error('Failed to fetch time entries');
    }
  },

  // Get single time entry
  getTimeById: async (id: string): Promise<TimeEntry> => {
    try {
      const response = await axios.get(`${API_BASE_URL}/get-time/${id}`);
      return response.data;
    } catch (error) {
      throw new Error('Failed to fetch time entry');
    }
  },

  // Update time entry
  updateTime: async (id: string, time: string): Promise<TimeEntry> => {
    try {
      const response = await axios.put(`${API_BASE_URL}/update-time/${id}`, { time });
      return response.data.updatedTime;
    } catch (error) {
      throw new Error('Failed to update time entry');
    }
  },

  // Delete time entry
  deleteTime: async (id: string): Promise<void> => {
    try {
      await axios.delete(`${API_BASE_URL}/delete-time/${id}`);
    } catch (error) {
      throw new Error('Failed to delete time entry');
    }
  }
};