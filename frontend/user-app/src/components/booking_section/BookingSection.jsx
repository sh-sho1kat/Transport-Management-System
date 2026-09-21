import React, { useState, useEffect } from 'react';
import { Calendar, Clock } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { LocationService } from '../../services/LocationService';
import { TimeService } from '../../services/TimeService';
import { TripService } from '../../services/TripService';
import BackgroundImage from "../../assets/bg1.jpg";

const BusTicketBooking = () => {
  const navigate = useNavigate();
  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");
  const [date, setDate] = useState("");
  const [departureTime, setDepartureTime] = useState("");
  const [locations, setLocations] = useState([]);
  const [timeSlots, setTimeSlots] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchStatus, setSearchStatus] = useState(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setIsLoading(true);
        const [locationsData, timeSlotsData] = await Promise.all([
          LocationService.getAllLocations(),
          TimeService.getAllTimes()
        ]);
        setLocations(locationsData);
        setTimeSlots(timeSlotsData);
      } catch (err) {
        setError('Failed to load booking data. Please try again later.');
      } finally {
        setIsLoading(false);
      }
    };

    fetchData();
  }, []);

  const formatTime = (time) => {
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

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSearchStatus(null);

    // Basic validation
    if (!from || !to || !date || !departureTime) {
      setSearchStatus({
        type: 'error',
        message: 'Please fill in all fields'
      });
      return;
    }

    try {
      // Get all trips and filter for matching criteria
      const trips = await TripService.getAllTrips();
      const matchingTrip = trips.find(trip => 
        trip.startlocation === from &&
        trip.destination === to &&
        //trip.date === date &&
        trip.departuretime === departureTime,
      );

      if (matchingTrip) {
        // Navigate to buses page with trip details
        navigate(`/buses`, { 
          state: { 
            tripID: matchingTrip.tripID
          }
        });
        setSearchStatus({
          type: 'error',
          message: 'Route was Found'
        });
      } else {
        setSearchStatus({
          type: 'error',
          message: 'No buses found for the selected route and time'
        });
      }
    } catch (err) {
      setSearchStatus({
        type: 'error',
        message: 'Error searching for buses. Please try again.'
      });
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-screen">
        <div className="text-white">Loading booking information...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center h-screen">
        <div className="text-red-500">{error}</div>
      </div>
    );
  }

  return (
    <div className="relative w-full h-[calc(100vh-64px)]">
      <div 
        className="absolute inset-0 bg-cover bg-center"
        style={{
          backgroundImage: `url(${BackgroundImage})`,
          filter: "brightness(50%)"
        }}
      ></div>
      
      <div className="relative z-10 flex flex-col items-center justify-center h-full text-white px-4 py-12">
        <h1 className="text-4xl md:text-5xl font-bold mb-4 text-center">BOOK YOUR BUS TICKET</h1>
        <p className="text-lg md:text-xl mb-8 text-center">Choose Your Destinations And Dates To Reserve A Ticket</p>

        {searchStatus && (
          <div className={`mb-4 w-full max-w-4xl p-4 rounded-md ${searchStatus.type === 'error' ? 'bg-red-500' : 'bg-green-500'} text-white`}>
            {searchStatus.message}
          </div>
        )}

        <form onSubmit={handleSubmit} className="bg-gray-900 p-6 rounded-lg shadow-md w-full max-w-4xl border border-gray-700">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
            <div className="space-y-2">
              <label className="text-sm text-gray-300">From</label>
              <select
                value={from}
                onChange={(e) => setFrom(e.target.value)}
                className="w-full px-3 py-2 bg-gray-800 border border-gray-700 rounded-md text-white focus:outline-none focus:ring-2 focus:ring-green-500"
                required
              >
                <option value="">Select departure city</option>
                {locations.map((loc) => (
                  <option key={loc._id} value={loc.location}>
                    {loc.location}
                  </option>
                ))}
              </select>
            </div>

            <div className="space-y-2">
              <label className="text-sm text-gray-300">To</label>
              <select
                value={to}
                onChange={(e) => setTo(e.target.value)}
                className="w-full px-3 py-2 bg-gray-800 border border-gray-700 rounded-md text-white focus:outline-none focus:ring-2 focus:ring-green-500"
                required
              >
                <option value="">Select destination city</option>
                {locations.map((loc) => (
                  <option key={loc._id} value={loc.location}>
                    {loc.location}
                  </option>
                ))}
              </select>
            </div>

            <div className="space-y-2">
              <label className="text-sm text-gray-300">Departure Date</label>
              <input
                type="date"
                value={date}
                onChange={(e) => setDate(e.target.value)}
                className="w-full px-3 py-2 bg-gray-800 border border-gray-700 rounded-md text-white focus:outline-none focus:ring-2 focus:ring-green-500"
                required
              />
            </div>

            <div className="space-y-2">
              <label className="text-sm text-gray-300">Departure Time</label>
              <select
                value={departureTime}
                onChange={(e) => setDepartureTime(e.target.value)}
                className="w-full px-3 py-2 bg-gray-800 border border-gray-700 rounded-md text-white focus:outline-none focus:ring-2 focus:ring-green-500"
                required
              >
                <option value="">Select departure time</option>
                {timeSlots.map((slot) => (
                  <option key={slot._id} value={slot.time}>
                  {formatTime(slot.time)}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <button 
            type="submit" 
            className="w-full px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded-md transition duration-300 focus:outline-none focus:ring-2 focus:ring-green-500"
          >
            Find Tickets
          </button>
        </form>
      </div>
    </div>
  );
};

export default BusTicketBooking;
