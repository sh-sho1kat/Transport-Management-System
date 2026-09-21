import React, { useState } from 'react';
import Breadcrumb from '../../components/Breadcrumbs/Breadcrumb';
import { TimeService } from '../../services/TimeService';
import TimeList from './TimeList';

const AddTime = () => {
  const [time, setTime] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccess('');

    try {
      await TimeService.createTime(time);
      setSuccess('Time entry created successfully!');
      setTime(''); // Reset form
      // The TimeList component will automatically refresh due to its useEffect
    } catch (err) {
      setError('Failed to create time entry. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <Breadcrumb pageName="Add Time" />

      <div className="grid grid-cols-1 gap-9">
        {/* Add Time Form */}
        <div className="flex flex-col gap-9">
          <div className="rounded-sm border border-stroke bg-white shadow-default dark:border-strokedark dark:bg-boxdark">
            <div className="border-b border-stroke py-4 px-6.5 dark:border-strokedark">
              <h3 className="font-medium text-black dark:text-white">
                Add Time Entry
              </h3>
            </div>
            
            <form action="#" onSubmit={handleSubmit}>
              <div className="p-6.5">
                {error && (
                  <div className="mb-4 text-meta-1">{error}</div>
                )}
                {success && (
                  <div className="mb-4 text-meta-3">{success}</div>
                )}

                <div className="mb-4.5">
                  <label className="mb-2.5 block text-black dark:text-white">
                    Time <span className="text-meta-1">*</span>
                  </label>
                  <input
                    type="time"
                    value={time}
                    onChange={(e) => setTime(e.target.value)}
                    required
                    disabled={loading}
                    className="w-full rounded border-[1.5px] border-stroke bg-transparent py-3 px-5 text-black outline-none transition focus:border-primary active:border-primary disabled:cursor-default disabled:bg-whiter dark:border-form-strokedark dark:bg-form-input dark:text-white dark:focus:border-primary"
                  />
                </div>

                <button 
                  type="submit"
                  disabled={loading}
                  className="flex w-full justify-center rounded bg-primary p-3 font-medium text-gray hover:bg-opacity-90 disabled:opacity-50"
                >
                  {loading ? 'Submitting...' : 'Submit Time'}
                </button>
              </div>
            </form>
          </div>
        </div>

        {/* Time List */}
        <TimeList />
      </div>
    </>
  );
};

export default AddTime;