import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Navbar from './components/navbar/Navbar';
import Footer from './components/footer/Footer';
import BookingSection from './components/booking_section/BookingSection';
import About from './components/about/About';
import Buses from './components/buses/Buses';
import Route_schedule from './components/route_schedule/Route_schedule';

function App() {
  return (
    <>
      <Router>
        <div className='w-full min-h-screen bg-neutral-50 dark:bg-neutral-950 text-neutral-800 dark:text-neutral-300 flex flex-col overflow-hidden'>
          {/* Navbar */}
          <Navbar />

          {/* Routes */}
          <Routes>
            <Route path="/" element={<BookingSection />} />
            <Route path="/home" element={<BookingSection />} />
            <Route path="/about" element={<About />} />
            <Route path="/buses" element={<Buses />} />
            <Route path="/route_schedule" element={<Route_schedule />} />
          </Routes>

          {/* Footer */}
          <Footer />
        </div>
      </Router>
    </>
  );
}

export default App;