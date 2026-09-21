import React, { useState, useEffect } from 'react';
import moment from 'moment';
import { TripService, TripData } from '../../services/TripService';
import { SeatBookService } from '../../services/SeatBookService';

const TripSeatDashboard: React.FC = () => {
  const [stats, setStats] = useState({
    totalTrips: 0,
    totalSeats: 0,
    bookedSeats: 0,
    availableSeats: 0,
  });
  
  const [trips, setTrips] = useState<TripData[]>([]);
  const [selectedTripId, setSelectedTripId] = useState<string>('');
  const [seats, setSeats] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [selectedSeat, setSelectedSeat] = useState(null);
  const [editMode, setEditMode] = useState(false);
  
  // Form state for editing
  const [editForm, setEditForm] = useState({
    bookingStatus: 'unbooked',
    studentId: '',
    studentMail: '',
  });

  // Fetch trips and initial stats on component mount
  useEffect(() => {
    const fetchInitialData = async () => {
      try {
        // Fetch all trips
        const fetchedTrips = await TripService.getAllTrips();
        setTrips(fetchedTrips);

        // Fetch stats
        await fetchStats();
      } catch (error) {
        setError('Failed to fetch initial data');
      }
    };

    fetchInitialData();
  }, []);

  const fetchStats = async () => {
    try {
      const trips = await TripService.getAllTrips();
      const totalTrips = trips.length;
      
      let totalSeats = 0;
      let bookedSeats = 0;
      
      await Promise.all(trips.map(async (trip) => {
        const seats = await SeatBookService.getAllSeats(trip.tripID);
        totalSeats += seats.length;
        const booked = seats.filter(seat => seat.bookingStatus === 'booked').length;
        bookedSeats += booked;
      }));

      setStats({
        totalTrips,
        totalSeats,
        bookedSeats,
        availableSeats: totalSeats - bookedSeats,
      });
    } catch (error) {
      setError('Failed to fetch statistics');
    }
  };

  const fetchSeats = async (tripId: string) => {
    if (!tripId) {
      setError('Please select a Trip');
      return;
    }
    
    setLoading(true);
    setError('');
    try {
      const seatsData = await SeatBookService.getAllSeats(tripId);
      setSeats(seatsData);
      setSelectedTripId(tripId);
    } catch (error) {
      setError('Failed to fetch seats for this trip');
    } finally {
      setLoading(false);
    }
  };

  const handleTripChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const tripId = e.target.value;
    fetchSeats(tripId);
  };

  const handleSeatEdit = (seat) => {
    setSelectedSeat(seat);
    setEditForm({
      bookingStatus: seat.bookingStatus,
      studentId: seat.studentId || '',
      studentMail: seat.studentMail || '',
    });
    setEditMode(true);
  };

  const handleSeatUpdate = async () => {
    try {
      await SeatBookService.updateSeat(selectedTripId, selectedSeat.seatNo, editForm);
      fetchSeats(selectedTripId); // Refresh seats data
      setEditMode(false);
      setSelectedSeat(null);
    } catch (error) {
      setError('Failed to update seat');
    }
  };
  

  return (
    <div className="p-4">
      {/* Stats Cards */}
      <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4 mb-6">
        <div className="rounded-sm border border-stroke bg-white p-4 dark:border-strokedark dark:bg-boxdark">
          <h4 className="text-title-md font-bold text-black dark:text-white">Total Trips</h4>
          <div className="mt-4 flex items-end justify-between">
            <div>
              <h4 className="text-title-md font-bold text-black dark:text-white">{stats.totalTrips}</h4>
            </div>
          </div>
        </div>

        <div className="rounded-sm border border-stroke bg-white p-4 dark:border-strokedark dark:bg-boxdark">
          <h4 className="text-title-md font-bold text-black dark:text-white">Total Seats</h4>
          <div className="mt-4 flex items-end justify-between">
            <div>
              <h4 className="text-title-md font-bold text-black dark:text-white">{stats.totalSeats}</h4>
            </div>
          </div>
        </div>

        <div className="rounded-sm border border-stroke bg-white p-4 dark:border-strokedark dark:bg-boxdark">
          <h4 className="text-title-md font-bold text-black dark:text-white">Booked Seats</h4>
          <div className="mt-4 flex items-end justify-between">
            <div>
              <h4 className="text-title-md font-bold text-black dark:text-white">{stats.bookedSeats}</h4>
            </div>
          </div>
        </div>

        <div className="rounded-sm border border-stroke bg-white p-4 dark:border-strokedark dark:bg-boxdark">
          <h4 className="text-title-md font-bold text-black dark:text-white">Available Seats</h4>
          <div className="mt-4 flex items-end justify-between">
            <div>
              <h4 className="text-title-md font-bold text-black dark:text-white">{stats.availableSeats}</h4>
            </div>
          </div>
        </div>
      </div>

      {/* Trip Selection Dropdown */}
      <div className="mb-6">
        <div className="flex gap-4">
          <select
            value={selectedTripId}
            onChange={handleTripChange}
            className="w-full rounded border border-stroke px-3 py-2 text-black focus:border-primary focus:outline-none dark:border-strokedark dark:bg-meta-4 dark:text-white dark:focus:border-primary"
          >
            <option value="">Select a Trip</option>
            {trips.map((trip) => (
              <option key={trip.tripID} value={trip.tripID}>
                {`${trip.tripID} - ${trip.startlocation} to ${trip.destination} (${moment(trip.date).format('MMMM D, YYYY h:mm A')})`}
              </option>
            ))}
          </select>
        </div>
        {error && <p className="mt-2 text-red-500">{error}</p>}
      </div>


      {/* Seats Display */}
      {loading ? (
        <div className="text-center">Loading...</div>
      ) : (
        <div className="rounded-sm border border-stroke bg-white px-5 pt-6 pb-2.5 dark:border-strokedark dark:bg-boxdark sm:px-7.5 xl:pb-1">
          <div className="max-w-full overflow-x-auto">
            <table className="w-full table-auto">
              <thead>
                <tr className="bg-gray-2 text-left dark:bg-meta-4">
                  <th className="py-4 px-4 font-medium text-black dark:text-white">Seat No</th>
                  <th className="py-4 px-4 font-medium text-black dark:text-white">Status</th>
                  <th className="py-4 px-4 font-medium text-black dark:text-white">Student ID</th>
                  <th className="py-4 px-4 font-medium text-black dark:text-white">Student Email</th>
                  <th className="py-4 px-4 font-medium text-black dark:text-white">Actions</th>
                </tr>
              </thead>
              <tbody>
                {seats.map((seat) => (
                  <tr key={seat._id}>
                    <td className="border-b border-[#eee] py-5 px-4 dark:border-strokedark">
                      {seat.seatNo}
                    </td>
                    <td className="border-b border-[#eee] py-5 px-4 dark:border-strokedark">
                      <span className={`inline-block rounded px-2.5 py-0.5 text-sm font-medium ${
                        seat.bookingStatus === 'booked' ? 'bg-red-100 text-red-600' : 'bg-green-100 text-green-600'
                      }`}>
                        {seat.bookingStatus}
                      </span>
                    </td>
                    <td className="border-b border-[#eee] py-5 px-4 dark:border-strokedark">
                      {seat.studentId || '-'}
                    </td>
                    <td className="border-b border-[#eee] py-5 px-4 dark:border-strokedark">
                      {seat.studentMail || '-'}
                    </td>
                    <td className="border-b border-[#eee] py-5 px-4 dark:border-strokedark">
                      <button
                        onClick={() => handleSeatEdit(seat)}
                        className="inline-flex items-center justify-center rounded-md border border-primary py-2 px-4 text-center font-medium text-primary hover:bg-opacity-90"
                      >
                        Edit
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Edit Modal */}
      {editMode && selectedSeat && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center">
          <div className="bg-white p-6 rounded-lg w-96">
            <h2 className="text-xl font-bold mb-4">Edit Seat {selectedSeat.seatNo}</h2>
            
            <div className="mb-4">
              <label className="block text-sm font-medium mb-1">Booking Status</label>
              <select
                value={editForm.bookingStatus}
                onChange={(e) => setEditForm({...editForm, bookingStatus: e.target.value})}
                className="w-full rounded border border-stroke px-3 py-2"
              >
                <option value="booked">Booked</option>
                <option value="unbooked">Unbooked</option>
              </select>
            </div>

            <div className="mb-4">
              <label className="block text-sm font-medium mb-1">Student ID</label>
              <input
                type="text"
                value={editForm.studentId}
                onChange={(e) => setEditForm({...editForm, studentId: e.target.value})}
                className="w-full rounded border border-stroke px-3 py-2"
              />
            </div>

            <div className="mb-4">
              <label className="block text-sm font-medium mb-1">Student Email</label>
              <input
                type="email"
                value={editForm.studentMail}
                onChange={(e) => setEditForm({...editForm, studentMail: e.target.value})}
                className="w-full rounded border border-stroke px-3 py-2"
              />
            </div>

            <div className="flex justify-end gap-4">
              <button
                onClick={() => {
                  setEditMode(false);
                  setSelectedSeat(null);
                }}
                className="rounded bg-gray-300 px-4 py-2 font-medium text-gray-700 hover:bg-opacity-90"
              >
                Cancel
              </button>
              <button
                onClick={handleSeatUpdate}
                className="rounded bg-primary px-4 py-2 font-medium text-white hover:bg-opacity-90"
              >
                Save Changes
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default TripSeatDashboard;