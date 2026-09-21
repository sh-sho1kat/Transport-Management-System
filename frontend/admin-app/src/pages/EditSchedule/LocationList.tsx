import React, { useState, useEffect } from 'react';
import { LocationService, LocationEntry } from '../../services/LocationService';

const LocationList: React.FC = () => {
  const [locations, setLocations] = useState<LocationEntry[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    fetchLocations();
  }, []);

  const fetchLocations = async () => {
    try {
      setLoading(true);
      const locationEntries = await LocationService.getAllLocations();
      setLocations(locationEntries);
    } catch (err) {
      setError('Failed to fetch locations');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: string) => {
    if (window.confirm('Are you sure you want to delete this location?')) {
      try {
        setLoading(true);
        await LocationService.deleteLocation(id);
        setSuccess('Location deleted successfully');
        fetchLocations();
      } catch (err) {
        setError('Failed to delete location');
      } finally {
        setLoading(false);
      }
    }
  };

  if (loading && !locations.length) {
    return <div className="p-4">Loading...</div>;
  }

  return (
    <div className="rounded-sm border border-stroke bg-white shadow-default dark:border-strokedark dark:bg-boxdark">
      <div className="border-b border-stroke py-4 px-6.5 dark:border-strokedark">
        <h3 className="font-medium text-black dark:text-white">
          Locations
        </h3>
      </div>

      {error && (
        <div className="p-6.5 text-meta-1">{error}</div>
      )}
      {success && (
        <div className="p-6.5 text-meta-3">{success}</div>
      )}

      <div className="p-6.5">
        {locations.length === 0 ? (
          <div className="text-center text-gray-500 dark:text-gray-400">
            No locations found
          </div>
        ) : (
          <div className="grid gap-4">
            {locations.map((locationEntry) => (
              <div 
                key={locationEntry._id}
                className="flex items-center justify-between p-4 rounded-sm border border-stroke dark:border-strokedark"
              >
                <span className="text-black dark:text-white">
                  {locationEntry.location}
                </span>
                <button
                  onClick={() => locationEntry._id && handleDelete(locationEntry._id)}
                  disabled={loading}
                  className="text-meta-1 hover:text-meta-1/80 disabled:opacity-50"
                  title="Delete location"
                >
                  Delete
                </button>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default LocationList;