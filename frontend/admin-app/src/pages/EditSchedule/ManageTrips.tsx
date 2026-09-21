import React, { useState, useEffect } from 'react';
import { TripService, TripData } from '../../services/TripService';
import DatePickerOne from '../../components/Forms/DatePicker/DatePickerThree';
import { LocationService } from '../../services/LocationService';
import { TimeService } from '../../services/TimeService';

const ManageTrips: React.FC = () => {
  const [trips, setTrips] = useState<TripData[]>([]);
  const [locations, setLocations] = useState<any[]>([]);
  const [times, setTimes] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [editingTrip, setEditingTrip] = useState<TripData | null>(null);

  useEffect(() => {
    fetchTrips();
    fetchLocationsAndTimes();
  }, []);

  const fetchTrips = async () => {
    try {
      setLoading(true);
      const data = await TripService.getAllTrips();
      setTrips(data);
    } catch (err) {
      setError('Failed to fetch trips');
    } finally {
      setLoading(false);
    }
  };

  const fetchLocationsAndTimes = async () => {
    try {
      const [locationData, timeData] = await Promise.all([
        LocationService.getAllLocations(),
        TimeService.getAllTimes()
      ]);
      setLocations(locationData);
      setTimes(timeData);
    } catch (err) {
      setError('Failed to fetch locations and times');
    }
  };

  const handleDelete = async (_id: string) => {
    if (!window.confirm('Are you sure you want to delete this trip?')) return;
    
    try {
      await TripService.deleteTrip(_id);
      setSuccess('Trip deleted successfully');
      fetchTrips();
    } catch (err) {
      setError('Failed to delete trip');
    }
  };

  const handleEdit = (trip: TripData) => {
    setEditingTrip(trip);
  };

  const handleUpdate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingTrip) return;

    try {
      await TripService.updateTrip(editingTrip._id as string, editingTrip);
      setSuccess('Trip updated successfully');
      setEditingTrip(null);
      fetchTrips();
    } catch (err) {
      setError('Failed to update trip');
    }
  };

  const formatTime = (time: string) => {
    const [hours, minutes] = time.split(':');
    const date = new Date();
    date.setHours(parseInt(hours));
    date.setMinutes(parseInt(minutes));
    return date.toLocaleTimeString('en-US', {
      hour: 'numeric',
      minute: '2-digit',
      hour12: true
    });
  };

  return (
    <div className="rounded-sm border border-stroke bg-white shadow-default dark:border-strokedark dark:bg-boxdark">
      <div className="border-b border-stroke py-4 px-6.5 dark:border-strokedark">
        <h3 className="font-medium text-black dark:text-white">Manage Trips</h3>
      </div>

      {error && <div className="p-6.5 text-meta-1">{error}</div>}
      {success && <div className="p-6.5 text-meta-3">{success}</div>}

      {editingTrip && (
        <div className="p-6.5">
          <form onSubmit={handleUpdate}>
            <div className="mb-4.5">
              <label className="mb-2.5 block text-black dark:text-white">
                Bus ID
              </label>
              <input
                type="text"
                value={editingTrip.busID}
                onChange={(e) => setEditingTrip({...editingTrip, busID: e.target.value})}
                className="w-full rounded border-[1.5px] border-stroke bg-transparent py-3 px-5 text-black dark:text-white"
              />
            </div>

            <div className="mb-4.5">
              <label className="mb-2.5 block text-black dark:text-white">
                Start Location
              </label>
              <select
                value={locations.find(loc => loc.location === editingTrip.startlocation)?._id}
                onChange={(e) => {
                  const location = locations.find(loc => loc._id === e.target.value);
                  setEditingTrip({...editingTrip, startlocation: location?.location || ''});
                }}
                className="w-full rounded border-[1.5px] border-stroke bg-transparent py-3 px-5 text-black dark:text-white"
              >
                {locations.map((loc) => (
                  <option key={loc._id} value={loc._id}>{loc.location}</option>
                ))}
              </select>
            </div>

            <div className="mb-4.5">
              <label className="mb-2.5 block text-black dark:text-white">
                Destination
              </label>
              <select
                value={locations.find(loc => loc.location === editingTrip.destination)?._id}
                onChange={(e) => {
                  const location = locations.find(loc => loc._id === e.target.value);
                  setEditingTrip({...editingTrip, destination: location?.location || ''});
                }}
                className="w-full rounded border-[1.5px] border-stroke bg-transparent py-3 px-5 text-black dark:text-white"
              >
                {locations.map((loc) => (
                  <option 
                    key={loc._id} 
                    value={loc._id}
                    disabled={loc.location === editingTrip.startlocation}
                  >
                    {loc.location}
                  </option>
                ))}
              </select>
            </div>

            <div className="mb-4.5">
              <label className="mb-2.5 block text-black dark:text-white">
                Date
              </label>
              <DatePickerOne
                selectedDate={new Date(editingTrip.date)}
                onChange={(newDate: Date) => setEditingTrip({...editingTrip, date: newDate.toISOString()})}
              />
            </div>

            <div className="mb-4.5">
              <label className="mb-2.5 block text-black dark:text-white">
                Departure Time
              </label>
              <select
                value={times.find(t => t.time === editingTrip.departuretime)?._id}
                onChange={(e) => {
                  const time = times.find(t => t._id === e.target.value);
                  setEditingTrip({...editingTrip, departuretime: time?.time || ''});
                }}
                className="w-full rounded border-[1.5px] border-stroke bg-transparent py-3 px-5 text-black dark:text-white"
              >
                {times.map((time) => (
                  <option key={time._id} value={time._id}>
                    {formatTime(time.time)}
                  </option>
                ))}
              </select>
            </div>

            <div className="flex gap-4">
              <button
                type="submit"
                className="flex justify-center rounded bg-primary p-3 font-medium text-gray"
              >
                Update Trip
              </button>
              <button
                type="button"
                onClick={() => setEditingTrip(null)}
                className="flex justify-center rounded bg-danger p-3 font-medium text-gray"
              >
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}

      <div className="p-6.5">
        {loading ? (
          <div>Loading...</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full table-auto">
              <thead>
                <tr className="bg-gray-2 text-left dark:bg-meta-4">
                  <th className="py-4 px-4 font-medium text-black dark:text-white">Trip ID</th>
                  <th className="py-4 px-4 font-medium text-black dark:text-white">Bus ID</th>
                  <th className="py-4 px-4 font-medium text-black dark:text-white">Start Location</th>
                  <th className="py-4 px-4 font-medium text-black dark:text-white">Destination</th>
                  <th className="py-4 px-4 font-medium text-black dark:text-white">Date</th>
                  <th className="py-4 px-4 font-medium text-black dark:text-white">Time</th>
                  <th className="py-4 px-4 font-medium text-black dark:text-white">Actions</th>
                </tr>
              </thead>
              <tbody>
                {trips.map((trip) => (
                  <tr key={trip._id}>
                    <td className="border-b border-[#eee] py-5 px-4 dark:border-strokedark">
                      {trip.tripID}
                    </td>
                    <td className="border-b border-[#eee] py-5 px-4 dark:border-strokedark">
                      {trip.busID}
                    </td>
                    <td className="border-b border-[#eee] py-5 px-4 dark:border-strokedark">
                      {trip.startlocation}
                    </td>
                    <td className="border-b border-[#eee] py-5 px-4 dark:border-strokedark">
                      {trip.destination}
                    </td>
                    <td className="border-b border-[#eee] py-5 px-4 dark:border-strokedark">
                      {new Date(trip.date).toLocaleDateString()}
                    </td>
                    <td className="border-b border-[#eee] py-5 px-4 dark:border-strokedark">
                      {formatTime(trip.departuretime)}
                    </td>
                    <td className="border-b border-[#eee] py-5 px-4 dark:border-strokedark">
                      <div className="flex gap-4">
                        <button
                          onClick={() => handleEdit(trip)}
                          className="hover:text-primary"
                        >
                          Edit
                        </button>
                        <button
                          onClick={() => trip._id && handleDelete(trip._id)}
                          className="hover:text-danger"
                        >
                          Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default ManageTrips;