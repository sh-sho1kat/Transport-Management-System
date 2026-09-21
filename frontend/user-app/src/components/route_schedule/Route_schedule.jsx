import React, { useState } from 'react';
import { Clock, MapPin, Bus, Info } from 'lucide-react';

const RouteSchedule = () => {
  const [activeFilter, setActiveFilter] = useState(null);

  const scheduleData = [
    {
      id: 1,
      users: "Students & Teachers (City Route)",
      route: "Shalbagan-Satellite Town-Shaheb Bazar-New Market-Monzur Mor-Kazla-RUET",
      departureRUET: ["9:00 AM", "10:00 AM", "12:30 PM", "3:00 PM"],
      departurePlace: [
        "Shalbagan: 9:10 AM",
        "Monjur Mor: 10:10 AM",
        "New Market: 12:40 PM",
        "Kazla: 3:10 PM"
      ],
      arrivalRUET: ["9:20 AM", "10:30 AM", "1:00 PM", "3:20 PM"],
      timeOfDay: ["morning", "morning", "noon", "evening"]
    },
    {
      id: 2,
      users: "Students & Teachers (Chandrima Route)",
      route: "Kazla-Baya Road-Chandrima-Abdulpur-RUET",
      departureRUET: ["9:00 AM", "1:00 PM"],
      departurePlace: ["Kazla: 9:10 AM", "Chandrima: 1:10 PM"],
      arrivalRUET: ["9:30 AM", "1:30 PM"],
      timeOfDay: ["morning", "noon"]
    },
    {
      id: 3,
      users: "Students & Teachers (Binodpur Route)",
      route: "Kazla-Binodpur-Biharir More-Kadirgonj-RUET",
      departureRUET: ["9:00 AM", "1:00 PM"],
      departurePlace: ["Binodpur: 9:10 AM", "Kadirgonj: 1:10 PM"],
      arrivalRUET: ["9:30 AM", "1:30 PM"],
      timeOfDay: ["morning", "noon"]
    },
    {
      id: 4,
      users: "Students & Teachers (Nawhata Route)",
      route: "Amanura-Sonadanga-Sialkol-Sagorpara-Sugonda-Kaligram-RUET",
      departureRUET: ["9:00 AM"],
      departurePlace: ["Amanura: 9:10 AM"],
      arrivalRUET: ["9:30 AM"],
      timeOfDay: ["morning"]
    },
    {
      id: 5,
      users: "Female Students (Residential Hall)",
      route: "RUET to Hall",
      departureRUET: ["9:45 AM", "10:00 AM", "1:00 PM", "2:15 PM"],
      departurePlace: [],
      arrivalRUET: [],
      timeOfDay: ["morning", "morning", "noon", "afternoon"]
    },
    {
      id: 6,
      users: "Students & Teachers (Campus Trip)",
      route: "RUET-Satmatha-Muktijoddha Mor-RUET",
      departureRUET: ["9:00 AM", "1:00 PM"],
      departurePlace: ["Satmatha: 9:10 AM", "Muktijoddha Mor: 1:10 PM"],
      arrivalRUET: ["9:20 AM", "1:20 PM"],
      timeOfDay: ["morning", "noon"]
    },
    {
      id: 7,
      users: "Campus Trip (RUET to RUET)",
      route: "RUET-Satmatha-RUET",
      departureRUET: ["9:00 AM"],
      departurePlace: ["Satmatha: 9:10 AM"],
      arrivalRUET: ["9:20 AM"],
      timeOfDay: ["morning"]
    }
  ];

  const timeFilters = [
    { type: "morning", label: "Morning Trips", color: "bg-blue-900" },
    { type: "noon", label: "Noon Trips", color: "bg-green-900" },
    { type: "afternoon", label: "Afternoon Trips", color: "bg-yellow-900" },
    { type: "evening", label: "Evening Trips", color: "bg-purple-900" }
  ];

  const filteredSchedule = activeFilter 
    ? scheduleData.filter(route => route.timeOfDay.includes(activeFilter))
    : scheduleData;

  return (
    <div className="min-h-screen bg-gray-900 text-gray-100 p-4 flex flex-col">
      <div className="max-w-6xl mx-auto w-full">
        <div className="bg-gray-800 rounded-xl shadow-2xl overflow-hidden">
          <div className="bg-blue-900 text-white p-6">
            <h1 className="text-3xl font-bold text-center flex items-center justify-center gap-3">
              <Bus size={36} />
              RUET Transportation Schedule
            </h1>
            <div className="text-center mt-2">
              <p className="font-medium">Administrative Branch, Transport Department</p>
              <p className="text-blue-300">Effective from: 20/10/2024</p>
            </div>
          </div>

          <div className="p-6">
            <div className="flex items-center justify-between mb-6">
              <div className="flex gap-2 items-center">
                <Info size={20} className="text-blue-500" />
                <p className="text-gray-300">
                  Schedules are subject to change. Please confirm timings regularly.
                </p>
              </div>
              <div className="flex gap-2">
                <button 
                  onClick={() => setActiveFilter(null)}
                  className={`px-3 py-1 rounded-full text-sm font-medium ${!activeFilter ? 'bg-blue-600 text-white' : 'bg-gray-700 text-gray-400'}`}
                >
                  All Trips
                </button>
                {timeFilters.map((timeFilter) => (
                  <button
                    key={timeFilter.type}
                    onClick={() => setActiveFilter(timeFilter.type)}
                    className={`px-3 py-1 rounded-full text-sm font-medium ${activeFilter === timeFilter.type ? 'bg-blue-600 text-white' : 'bg-gray-700 text-gray-400'}`}
                  >
                    {timeFilter.label}
                  </button>
                ))}
              </div>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full border-collapse">
                <thead>
                  <tr className="bg-gray-700">
                    <th className="p-3 text-left font-semibold border border-gray-600">Users</th>
                    <th className="p-3 text-left font-semibold border border-gray-600">Route</th>
                    <th className="p-3 text-left font-semibold border border-gray-600">
                      <Clock size={16} className="inline mr-2" />
                      Departure (RUET)
                    </th>
                    <th className="p-3 text-left font-semibold border border-gray-600">
                      <MapPin size={16} className="inline mr-2" />
                      Departure (Location)
                    </th>
                    <th className="p-3 text-left font-semibold border border-gray-600">
                      <Clock size={16} className="inline mr-2" />
                      Arrival (RUET)
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {filteredSchedule.map((schedule) => (
                    <tr 
                      key={schedule.id} 
                      className="hover:bg-gray-700 transition-colors"
                    >
                      <td className="p-3 border border-gray-600 font-medium">{schedule.users}</td>
                      <td className="p-3 border border-gray-600">{schedule.route}</td>
                      <td className="p-3 border border-gray-600">
                        {schedule.departureRUET.map((time, i) => (
                          <div key={i} className="flex items-center gap-2">
                            <Clock size={14} className="text-blue-500" />
                            {time}
                          </div>
                        ))}
                      </td>
                      <td className="p-3 border border-gray-600">
                        {schedule.departurePlace.length > 0 ? (
                          schedule.departurePlace.map((place, i) => (
                            <div key={i} className="flex items-center gap-2">
                              <MapPin size={14} className="text-green-500" />
                              {place}
                            </div>
                          ))
                        ) : (
                          <span className="text-gray-500">No specific locations</span>
                        )}
                      </td>
                      <td className="p-3 border border-gray-600">
                        {schedule.arrivalRUET.map((time, i) => (
                          <div key={i} className="flex items-center gap-2">
                            <Clock size={14} className="text-blue-500" />
                            {time}
                          </div>
                        ))}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default RouteSchedule;