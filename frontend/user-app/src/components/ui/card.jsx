// components/ui/card.jsx
import React from 'react';

export const Card = React.forwardRef(({ className = "", children, ...props }, ref) => {
  return (
    <div
      ref={ref}
      className={`rounded-lg border bg-card text-card-foreground shadow-sm ${className}`}
      {...props}
    >
      {children}
    </div>
  );
});

Card.displayName = "Card";