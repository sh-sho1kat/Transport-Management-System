// components/ui/button.jsx
import React from 'react';

export const Button = React.forwardRef(({ 
  className = "", 
  children, 
  disabled, 
  type = "button",
  ...props 
}, ref) => {
  return (
    <button
      type={type}
      ref={ref}
      disabled={disabled}
      className={`inline-flex items-center justify-center rounded-md font-medium transition-colors 
        focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 
        disabled:pointer-events-none disabled:opacity-50 ${className}`}
      {...props}
    >
      {children}
    </button>
  );
});

Button.displayName = "Button";