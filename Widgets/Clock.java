package Widgets;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Encapsulates a single, independent clock which will count repeatedly to
 * its given target time in milliseconds and, after achieving that time,
 * fire to its listeners a {@link ClockEvent}.
 * <p>
 * Each Clock comes with a single {@link ClockThread} which ticks every
 * 1 millisecond. Pausing the Clock will prevent the {@link ClockThread}
 * from incrementing {@link #accumulated} time, but will not stop the
 * Thread. Stopping the Clock will terminate the {@link ClockThread}.
 * Starting the Clock will stop any exisiting {@link ClockThread} and
 * create a new one.
 * 
 * @author Nius Atreides (Nicholas Harrell)
 */
public class Clock
{
	/** The total accumulated (elapsed) time for this Clock. */
	protected int accumulated = 0;
	
	/** This Clock's one and only counting thread, which iterates
		the {@link #accumulated} time while {@link ClockThread#canRun} is true. */
	protected ClockThread CT;
	
	/** The time in milliseconds which must elapse before firing the
	 	{@link ClockEvent}. */
	public final int EVENT_INTERVAL;
	
	/**
	 * Constructs a new Clock object with the specified interval.
	 * 
	 * @param interval	The number of milliseconds in each clock cycle.
	 */
	public Clock(int interval)
	{
		EVENT_INTERVAL = interval;
	}
	
	/**
	 * Starts a new {@link ClockThread} and causes the Clock to begin tracking
	 * {@link #accumulated} time. Starting the Clock after it is already running
	 * will terminate the existing {@link ClockThread} by calling {@link #stopClock()}
	 * and create a new one, but the {@link #accumulated} time elapsed will remain unchanged.
	 * <p>
	 * Clocks with an {@link #EVENT_INTERVAL} less than 1 will never instantiate a
	 * {@link ClockThread}. A {@link ClockThread} with an interval less than 1 would be
	 * a useless thread and would cause some interesting null pointer exceptions.
	 * <p>
	 * Calling this method on a Clock with an {@link #EVENT_INTERVAL} of less than 1 will
	 * have no effect.
	 */
	public void startClock()
	{
		//If the interval is less than 1, do nothing.
		if(EVENT_INTERVAL < 1)
			return;
		
		if(CT != null)
			CT.canRun = false;
		CT = new ClockThread(1);
		CT.start();
	}
	
	/**
	 * Effectively pauses this Clock by preventing the running {@link ClockThread} from
	 * adding to the {@link #accumulated} time elapsed. The {@link ClockThread} is not
	 * halted.
	 * <p>
	 * Clocks with an {@link #EVENT_INTERVAL} less than 1 will never instantiate a
	 * {@link ClockThread}. A {@link ClockThread} with an interval less than 1 would be
	 * a useless thread and would cause some interesting null pointer exceptions.
	 * <p>
	 * Calling this method on a Clock with an {@link #EVENT_INTERVAL} of less than 1 will
	 * have no effect.
	 * <p>
	 * Calling this method on a Clock which is already paused will have no effect.
	 */
	public void pauseClock()
	{
		//If the interval is less than 1, do nothing.
		if(EVENT_INTERVAL < 1)
			return;
		
		/* DEBUG *///System.out.println("<CLK> Pausing a clock.");
		CT.canRun = false;
	}
	
	/**
	 * Halts the {@link ClockThread} and resets the {@link #accumulated} time elapsed to 0.
	 * <p>
	 * Clocks with an {@link #EVENT_INTERVAL} less than 1 will never instantiate a
	 * {@link ClockThread}. A {@link ClockThread} with an interval less than 1 would be
	 * a useless thread and would cause some interesting null pointer exceptions.
	 * <p>
	 * Calling this method on a Clock with an {@link #EVENT_INTERVAL} of less than 1 will
	 * have no effect.
	 * <p>
	 * Calling this method on a Clock which is already stopped will have no effect.
	 */
	public void stopClock()
	{
		//If the interval is less than 1, do nothing.
		if(EVENT_INTERVAL < 1)
			return;
		
		/* DEBUG *///System.out.println("<CLK> Stopping a clock.");
		pauseClock();
		accumulated = 0;
	}
	
	/**
	 * Increments the {@link #accumulated} time by <code>accum</code> milliseconds and checks
	 * the total accumulated time elapsed against the prescribed {@link #EVENT_INTERVAL}.
	 * If the time elapsed is equal to or greater than the {@link #EVENT_INTERVAL} then a
	 * {@link ClockEvent} will be fired.
	 * 
	 * @param accum The number of milliseconds by which to increment the {@link #accumulated} time.
	 */
	protected void accumulate(int accum)
	{
		accumulated += accum;
		if(accumulated >= EVENT_INTERVAL)
		{
			fireEvent();
			accumulated -= EVENT_INTERVAL;
		}
	}
	
	/**
	 * Encapsulates the functionality of a simple counting clock.
	 * This thread runs independently of any other, and will
	 * continuously tick every {@link #IVL} milliseconds until
	 * instructed to stop.
	 * 
	 * @author Nius Atreides (Nicholas Harrell)
	 */
	protected class ClockThread extends Thread
	{
		/** The number of milliseconds between ticks. */
		public final int IVL;
		
		/** Whether this ClockThread is permitted to tick. */
		public boolean canRun = true;
		
		/**
		 * Creates a new ClockThread object with the specified
		 * interval between ticks.
		 * 
		 * @param interval	The number of milliseconds to wait
		 * 					between ticks.
		 */
		public ClockThread(int interval)
		{
			super();
			IVL = interval;
		}
		
		/**
		 * Causes the ClockThread to begin repeatedly ticking,
		 * every {@link #IVL} seconds, until {@link #canRun} is
		 * <code>false</code>.
		 */
		public void run()
		{
			boolean firstRun = true;
			while(canRun)
			{
				if(firstRun)
					firstRun = false;
				else
					accumulate(IVL);
				try
				{
					Thread.sleep(IVL);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	//
	//	EVENT HANDLING
	//
	
	/** The list of objects listening for events from this Clock. */
	protected ArrayList<Object> listeners = new ArrayList<Object>();
	
	/**
	 * Adds the specified object to the list of listeners to this Clock.
	 * All objects on this list are notified when this Clock completes a cycle.
	 * 
	 * @param listener The object which will begin listening to this Clock.
	 */
	public synchronized void addClockListener(ClockListener listener)
	{
		listeners.add(listener);
	}
	
	/**
	 * Removes the specified object from the list of listeners to this Clock.
	 * The specified object will no longer be notified when this Clock completes
	 * a cycle.
	 * 
	 * @param listener	The object to remove from the list of listeners to this Clock.
	 */
	public synchronized void removeClockListener(ClockListener listener)
	{
		listeners.remove(listener);
	}
	
	/**
	 * Notifies all listeners to this Clock that it has completed a cycle.
	 */
	private synchronized void fireEvent()
	{
	    ClockEvent event = new ClockEvent(this);
	    Iterator<Object> i = listeners.iterator();
	    while(i.hasNext())
	    {
	      ((ClockListener) i.next()).clockTicked(event);
	    }
	}
	
	/**
	 * Structures the listening behavior for objects listening to a
	 * {@link Clock}.
	 * 
	 * @author Nius Atreides (Nicholas Harrell)
	 */
	public interface ClockListener
	{
		/**
		 * This method is called when a {@link Clock} executes its
		 * {@link Clock#fireEvent()} method.
		 * 
		 * @param e		An event object representing the transaction.
		 */
		public void clockTicked(ClockEvent e);
	}
	
	/**
	 * A simple extension of a basic {@link java.util.EventObject}, facilitating standardized
	 * event data for time-driven inter-object transactions.
	 * 
	 * @author Nius Atreides (Nicholas Harrell)
	 */
	public class ClockEvent extends java.util.EventObject
	{
		/** Generic serial version ID */
		private static final long serialVersionUID = 1L;
		
		/**
		 * Creates a new default {@link java.util.EventObject} object extended to
		 * ClockEvent, carrying the specified object as the caller.
		 * 
		 * @param source	The object reference to carry as the caller.
		 */
		public ClockEvent(Object source)
		{
			super(source);
		}
		
		/**
		 * Returns the caller of this event as, specifically, a {@link Clock} object.
		 */
		@Override
		public Clock getSource()
		{
			return (Clock)source;
		}
	}
}
