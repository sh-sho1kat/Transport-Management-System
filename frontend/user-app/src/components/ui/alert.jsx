import React, { useState } from 'react';
import { X } from 'lucide-react';

const variants = {
  default: "bg-gray-100 border-gray-200 text-gray-800",
  error: "bg-red-50 border-red-200 text-red-800",
  warning: "bg-yellow-50 border-yellow-200 text-yellow-800",
  success: "bg-green-50 border-green-200 text-green-800",
  info: "bg-blue-50 border-blue-200 text-blue-800"
};

const iconVariants = {
  default: "text-gray-500",
  error: "text-red-500",
  warning: "text-yellow-500",
  success: "text-green-500",
  info: "text-blue-500"
};

export const Alert = React.forwardRef(({ 
  className = "", 
  variant = "default", 
  children,
  icon,
  dismissible = false,
  onDismiss,
  duration,
  ...props 
}, ref) => {
  const [isVisible, setIsVisible] = useState(true);
  const [isExiting, setIsExiting] = useState(false);

  React.useEffect(() => {
    let timer;
    if (duration) {
      timer = setTimeout(() => {
        handleDismiss();
      }, duration);
    }
    return () => clearTimeout(timer);
  }, [duration]);

  const handleDismiss = () => {
    setIsExiting(true);
    setTimeout(() => {
      setIsVisible(false);
      onDismiss?.();
    }, 300); // Match this with animation duration
  };

  if (!isVisible) return null;

  return (
    <div
      ref={ref}
      role="alert"
      aria-live="polite"
      className={`
        relative w-full rounded-lg border p-4 
        transition-all duration-300 ease-in-out
        ${isExiting ? 'opacity-0 scale-95' : 'opacity-100 scale-100'}
        ${variants[variant]} 
        ${className}
      `}
      {...props}
    >
      {icon && (
        <span className={`absolute left-4 top-4 ${iconVariants[variant]}`}>
          {icon}
        </span>
      )}
      
      <div className={`${icon ? 'pl-7' : ''} ${dismissible ? 'pr-7' : ''}`}>
        {children}
      </div>

      {dismissible && (
        <button
          onClick={handleDismiss}
          className={`
            absolute right-2 top-2 p-1 rounded-full 
            opacity-70 hover:opacity-100 transition-opacity
            focus:outline-none focus:ring-2 focus:ring-offset-2
            ${iconVariants[variant]} focus:ring-current
          `}
          aria-label="Dismiss alert"
        >
          <X className="h-4 w-4" />
        </button>
      )}
    </div>
  );
});

Alert.displayName = "Alert";

export const AlertDescription = React.forwardRef(({ 
  className = "", 
  children,
  ...props 
}, ref) => (
  <div
    ref={ref}
    className={`
      text-sm [&_p]:leading-relaxed
      ${className}
    `}
    {...props}
  >
    {children}
  </div>
));

AlertDescription.displayName = "AlertDescription";