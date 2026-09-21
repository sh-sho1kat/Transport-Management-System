import React, { useState, useEffect } from 'react';
import { TimeService, TimeEntry } from '../../services/TimeService';

const TimeList = () => {
  const [times, setTimes] = useState<TimeEntry[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Fetch all times when component mounts
  useEffect(() => {
    fetchTimes();
  }, []);

  const fetchTimes = async () => {
    try {
      setLoading(true);
      const timeEntries = await TimeService.getAllTimes();
      setTimes(timeEntries);
    } catch (err) {
      setError('Failed to fetch time entries');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: string) => {
    if (window.confirm('Are you sure you want to delete this time entry?')) {
      try {
        setLoading(true);
        await TimeService.deleteTime(id);
        setSuccess('Time entry deleted successfully');
        // Refresh the list after deletion
        fetchTimes();
      } catch (err) {
        setError('Failed to delete time entry');
      } finally {
        setLoading(false);
      }
    }
  };

  const formatTime = (time: string) => {
    // Convert 24-hour time to 12-hour format
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

  if (loading && !times.length) {
    return <div className="p-4">Loading...</div>;
  }

  return (
    <div className="rounded-sm border border-stroke bg-white shadow-default dark:border-strokedark dark:bg-boxdark">
      <div className="border-b border-stroke py-4 px-6.5 dark:border-strokedark">
        <h3 className="font-medium text-black dark:text-white">
          Time Entries
        </h3>
      </div>

      {error && (
        <div className="p-6.5 text-meta-1">{error}</div>
      )}
      {success && (
        <div className="p-6.5 text-meta-3">{success}</div>
      )}

      <div className="p-6.5">
        {times.length === 0 ? (
          <div className="text-center text-gray-500 dark:text-gray-400">
            No time entries found
          </div>
        ) : (
          <div className="grid gap-4">
            {times.map((timeEntry) => (
              <div 
                key={timeEntry._id}
                className="flex items-center justify-between p-4 rounded-sm border border-stroke dark:border-strokedark"
              >
                <span className="text-black dark:text-white">
                  {formatTime(timeEntry.time)}
                </span>
                <button
                  onClick={() => timeEntry._id && handleDelete(timeEntry._id)}
                  disabled={loading}
                  className="text-meta-1 hover:text-meta-1/80 disabled:opacity-50"
                  title="Delete time entry"
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

export default TimeList;