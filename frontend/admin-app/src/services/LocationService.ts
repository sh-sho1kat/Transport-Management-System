import axios from 'axios';


const API_BASE_URL = 'http://localhost:8000/api/admin';

export interface LocationEntry {
  _id?: string;
  location: string;
}

export const LocationService = {
  createLocation: async (location: string): Promise<LocationEntry> => {
    try {
      const response = await axios.post(`${API_BASE_URL}/create-location`, { location });
      return response.data.newLocation;
    } catch (error) {
      throw new Error('Failed to create location');
    }
  },

  getAllLocations: async (): Promise<LocationEntry[]> => {
    try {
      const response = await axios.get(`${API_BASE_URL}/get-location`);
      return response.data;
    } catch (error) {
      throw new Error('Failed to fetch locations');
    }
  },

  getLocationById: async (id: string): Promise<LocationEntry> => {
    try {
      const response = await axios.get(`${API_BASE_URL}/get-location/${id}`);
      return response.data;
    } catch (error) {
      throw new Error('Failed to fetch location');
    }
  },

  updateLocation: async (id: string, location: string): Promise<LocationEntry> => {
    try {
      const response = await axios.put(`${API_BASE_URL}/update-location/${id}`, { location });
      return response.data.updatedLocation;
    } catch (error) {
      throw new Error('Failed to update location');
    }
  },

  deleteLocation: async (id: string): Promise<void> => {
    try {
      await axios.delete(`${API_BASE_URL}/delete-location/${id}`);
    } catch (error) {
      throw new Error('Failed to delete location');
    }
  }
};