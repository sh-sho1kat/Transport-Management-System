import React, { useState, useEffect } from 'react';
import { useLocation, useParams } from 'react-router-dom';
import { Card } from '../ui/card';
import { Button } from '../ui/button';
import { Input } from '../ui/input';
import { Alert, AlertDescription } from '../ui/alert';
import { AlertCircle, Loader2 } from 'lucide-react';
import { SeatBookService } from '../../services/SeatBookService';

const Buses = () => {
  const { tripID } = useParams();
  const location = useLocation();
  const currentTripID = tripID || location.state?.tripID;

  const [selectedSeats, setSelectedSeats] = useState([]);
  const [allSeats, setAllSeats] = useState([]);
  const [loading, setLoading] = useState(true);
  const [studentId, setStudentId] = useState('');
  const [email, setEmail] = useState('');
  const [bookingStatus, setBookingStatus] = useState(null);

  const TOTAL_ROWS = 10;

  useEffect(() => {
    if (!currentTripID) {
      setBookingStatus({
        type: 'error',
        message: 'No trip ID provided'
      });
      setLoading(false);
      return;
    }
    loadSeatData();
  }, [currentTripID]);

  const loadSeatData = async () => {
    try {
      setLoading(true);
      const seats = await SeatBookService.getAllSeats(currentTripID);
      setAllSeats(seats);
      console.log(seats);
    } catch (error) {
      setBookingStatus({
        type: 'error',
        message: 'Failed to load seat data. Please try again later.'
      });
    } finally {
      setLoading(false);
    }
  };

  const handleSeatClick = (seatId) => {
    const seatNo = (seatId + 1).toString().padStart(2, '0');
    const seat = allSeats.find(s => s.seatNo === seatNo);

    if (seat?.bookingStatus === 'booked') {
      return;
    }

    setSelectedSeats(prev => {
      if (prev.includes(seatId)) {
        return prev.filter(id => id !== seatId);
      } else {
        if (prev.length >= 1) {
          setBookingStatus({
            type: 'error',
            message: 'You can only select up to 1 seats at a time.'
          });
          return prev;
        }
        return [...prev, seatId];
      }
    });
  };

  const handleBooking = async () => {
    if (!studentId.trim() || !email.trim()) {
      setBookingStatus({
        type: 'error',
        message: 'Please fill in both Student ID and Email'
      });
      return;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      setBookingStatus({
        type: 'error',
        message: 'Please enter a valid email address'
      });
      return;
    }

    if (studentId.length < 5) {
      setBookingStatus({
        type: 'error',
        message: 'Please enter a valid Student ID'
      });
      return;
    }

    try {
      setLoading(true);
      const currentDate = new Date();
      const currentTime = currentDate.toLocaleTimeString();

      const seatsToUpdate = selectedSeats.map(seatId => ({
        seatNo: (seatId + 1).toString().padStart(2, '0'),
        bookingStatus: 'booked',
        studentId: studentId,
        studentMail: email,
        bookingDate: currentDate,
        bookingTime: currentTime
      }));

      await SeatBookService.updateMultipleSeats(currentTripID, seatsToUpdate);
      await loadSeatData();
      
      setSelectedSeats([]);
      setStudentId('');
      setEmail('');
      setBookingStatus({
        type: 'success',
        message: 'Booking confirmed! Details will be sent to your email.'
      });
    } catch (error) {
      setBookingStatus({
        type: 'error',
        message: 'Failed to process booking. Please try again later.'
      });
    } finally {
      setLoading(false);
    }
  };

  const getSeatStatus = (seatId) => {
    const seatNo = (seatId + 1).toString().padStart(2, '0');
    const seat = allSeats.find(s => s.seatNo === seatNo);
    return seat?.bookingStatus || 'unbooked';
  };

  const getSeatColor = (seatId) => {
    const status = getSeatStatus(seatId);
    if (status === 'booked') return 'bg-red-500';
    if (selectedSeats.includes(seatId)) return 'bg-green-500';
    return 'bg-gray-200 hover:bg-gray-300';
  };

  const getSeatInfo = (seatId) => {
    const seatNo = (seatId + 1).toString().padStart(2, '0');
    return allSeats.find(s => s.seatNo === seatNo);
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin" />
      </div>
    );
  }

  if (!currentTripID) {
    return (
      <div className="min-h-screen p-8">
        <Alert className="bg-red-100">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>
            No trip ID provided. Please select a trip first.
          </AlertDescription>
        </Alert>
      </div>
    );
  }

  return (
    <div className="min-h-screen p-8">
      <div className="max-w-4xl mx-auto">
        <h1 className="text-3xl font-bold mb-8 text-center">University Bus Seat Booking</h1>
        
        {bookingStatus && (
          <Alert className={`mb-4 ${bookingStatus.type === 'error' ? 'bg-red-100' : 'bg-green-100'}`}>
            <AlertCircle className="h-4 w-4" />
            <AlertDescription>
              {bookingStatus.message}
            </AlertDescription>
          </Alert>
        )}
        
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          {/* Seat Layout */}
          <Card className="p-6">
            <h2 className="text-xl font-semibold mb-4">Select Your Seats</h2>
            <div className="space-y-4">
              <div className="w-full h-12 flex justify-end mb-8">
                <div className="w-12 h-12 bg-gray-800 rounded-lg flex items-center justify-center text-white text-sm">
                  Driver
                </div>
              </div>

              <div className="grid gap-4">
                {Array.from({ length: TOTAL_ROWS }).map((_, rowIndex) => (
                  <div key={rowIndex} className="flex justify-between">
                    {/* Left side seats */}
                    <div className="flex gap-2">
                      {[0, 1].map(seatNum => {
                        const seatId = rowIndex * 4 + seatNum;
                        const seatInfo = getSeatInfo(seatId);
                        const displaySeatNo = (seatId + 1).toString().padStart(2, '0');
                        return (
                          <button
                            key={`left-${seatNum}`}
                            onClick={() => handleSeatClick(seatId)}
                            disabled={seatInfo?.bookingStatus === 'booked'}
                            className={`
                              w-12 h-12 rounded-lg transition-colors duration-200
                              flex items-center justify-center font-medium
                              ${getSeatColor(seatId)}
                              ${seatInfo?.bookingStatus === 'booked' ? 'cursor-not-allowed' : 'cursor-pointer'}
                            `}
                            title={seatInfo?.bookingStatus === 'booked' ? `Booked by ${seatInfo.studentId}` : `Seat ${displaySeatNo}`}
                          >
                            {displaySeatNo}
                          </button>
                        );
                      })}
                    </div>

                    <div className="w-8"></div>

                    {/* Right side seats */}
                    <div className="flex gap-2">
                      {[2, 3].map(seatNum => {
                        const seatId = rowIndex * 4 + seatNum;
                        const seatInfo = getSeatInfo(seatId);
                        const displaySeatNo = (seatId + 1).toString().padStart(2, '0');
                        return (
                          <button
                            key={`right-${seatNum}`}
                            onClick={() => handleSeatClick(seatId)}
                            disabled={seatInfo?.bookingStatus === 'booked'}
                            className={`
                              w-12 h-12 rounded-lg transition-colors duration-200
                              flex items-center justify-center font-medium
                              ${getSeatColor(seatId)}
                              ${seatInfo?.bookingStatus === 'booked' ? 'cursor-not-allowed' : 'cursor-pointer'}
                            `}
                            title={seatInfo?.bookingStatus === 'booked' ? `Booked by ${seatInfo.studentId}` : `Seat ${displaySeatNo}`}
                          >
                            {displaySeatNo}
                          </button>
                        );
                      })}
                    </div>
                  </div>
                ))}
              </div>

              <div className="flex justify-center gap-4 mt-6">
                <div className="flex items-center gap-2">
                  <div className="w-4 h-4 bg-gray-200 rounded"></div>
                  <span>Available</span>
                </div>
                <div className="flex items-center gap-2">
                  <div className="w-4 h-4 bg-green-500 rounded"></div>
                  <span>Selected</span>
                </div>
                <div className="flex items-center gap-2">
                  <div className="w-4 h-4 bg-red-500 rounded"></div>
                  <span>Booked</span>
                </div>
              </div>
            </div>
          </Card>

          {/* Booking Form */}
          <Card className="p-6">
            <h2 className="text-xl font-semibold mb-4">Booking Details</h2>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium mb-2">Selected Seats</label>
                <div className="py-2 px-3 bg-gray-100 rounded">
                  {selectedSeats.length > 0 
                    ? selectedSeats.map(id => (id + 1).toString().padStart(2, '0')).join(', ')
                    : 'No seats selected'}
                </div>
              </div>

              <div>
                <label htmlFor="studentId" className="block text-sm font-medium mb-2">Student ID</label>
                <Input
                  id="studentId"
                  type="text"
                  value={studentId}
                  onChange={(e) => setStudentId(e.target.value)}
                  placeholder="Enter your student ID"
                  className="w-full"
                />
              </div>

              <div>
                <label htmlFor="email" className="block text-sm font-medium mb-2">University Email</label>
                <Input
                  id="email"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="Enter your university email"
                  className="w-full"
                />
              </div>

              <div className="pt-4">
                <Button
                  onClick={handleBooking}
                  disabled={selectedSeats.length === 0 || loading}
                  className="w-full bg-green-600 hover:bg-green-700 text-white p-2 rounded disabled:bg-gray-400 disabled:cursor-not-allowed"
                >
                  {loading ? (
                    <Loader2 className="h-4 w-4 animate-spin mx-auto" />
                  ) : (
                    'Confirm Booking'
                  )}
                </Button>
              </div>
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
};

export default Buses;