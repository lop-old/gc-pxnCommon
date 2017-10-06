package com.poixson.utils.xScheduler.trigger;

import java.util.concurrent.TimeUnit;

import com.poixson.utils.xTime;
import com.poixson.utils.xTimeU;
import com.poixson.utils.exceptions.RequiredArgumentException;
import com.poixson.utils.xScheduler.xSchedulerTrigger;


public class TriggerInterval extends xSchedulerTrigger {

	private final xTime delay    = xTime.get();
	private final xTime interval = xTime.get();
	private volatile long last = Long.MIN_VALUE;

	private final Object updateLock = new Object();



	// builder
	public static TriggerInterval builder() {
		return new TriggerInterval();
	}
	public TriggerInterval() {
	}

	// long
	public TriggerInterval(final long interval, final TimeUnit unit) {
		this(
			interval,
			interval,
			unit
		);
	}
	public TriggerInterval(final long delay, final long interval, final TimeUnit unit) {
		this();
		this.setDelay(   delay,    unit);
		this.setInterval(interval, unit);
	}

	// string
	public TriggerInterval(final String intervalStr) {
		this(
			intervalStr,
			intervalStr
		);
	}
	public TriggerInterval(final String delayStr, final String intervalStr) {
		this();
		this.setDelay(delayStr);
		this.setInterval(intervalStr);
	}

	// xTime
	public TriggerInterval(final xTime interval) {
		this(
			interval,
			interval
		);
	}
	public TriggerInterval(final xTime delay, final xTime interval) {
		this();
		this.setDelay(delay);
		this.setInterval(interval);
	}



	private void validateValues(final long now) {
		synchronized(this.updateLock) {
			// check delay/interval values
			{
				final long delay    = this.delay.getMS();
				final long interval = this.interval.getMS();
				if (interval < 1L) {
					if (delay < 1L)
						throw new RequiredArgumentException("delay/interval");
					// swap delay to interval
					// and set no repeat
					this.interval.set(
						delay,
						xTimeU.MS
					);
					this.delay.set(
						0L,
						xTimeU.MS
					);
					this.setRunOnce();
				}
			}
			// first calculations
			{
				final long last     = this.last;
				final long delay    = this.delay.getMS();
				final long interval = this.interval.getMS();
				if (last == Long.MIN_VALUE) {
					this.last = (now + delay) - interval;
				}
			}
		}
	}
	@Override
	public long untilNextTrigger(final long now) {
		if (this.notEnabled())
			return Long.MIN_VALUE;
		synchronized(this.updateLock) {
			this.validateValues(now);
			if (this.notEnabled())
				return Long.MIN_VALUE;
			// calculate time until next trigger
			final long last     = this.last;
			final long interval = this.interval.getMS();
			final long sinceLast = now - last;
			final long untilNext = interval - sinceLast;
			// trigger now
			if (untilNext <= 0L) {
				// adjust last value (keeping sync with time)
				final long add =
					((long) Math.floor(
						((double)sinceLast) / ((double)interval)
					)) * interval;
				this.last = last + add;
				return 0L;
			}
			// sleep time
			return untilNext;
		}
	}



	// ------------------------------------------------------------------------------- //
	// trigger config



	public TriggerInterval setDelay(final long delay, final TimeUnit unit) {
		this.delay.set(
			delay,
			unit
		);
		return this;
	}
	public TriggerInterval setDelay(final String delayStr) {
		this.delay.set(delayStr);
		return this;
	}
	public TriggerInterval setDelay(final xTime delay) {
		this.delay.set(delay);
		return this;
	}



	public TriggerInterval setInterval(final long interval, final TimeUnit unit) {
		this.interval.set(
			interval,
			unit
		);
		return this;
	}
	public TriggerInterval setInterval(final String intervalStr) {
		this.interval.set(intervalStr);
		return this;
	}
	public TriggerInterval setInterval(final xTime interval) {
		this.interval.set(interval);
		return this;
	}



	// ------------------------------------------------------------------------------- //
	// overrides



	public TriggerInterval enable() {
		return (
			super.enable() == null
			? null
			: this
		);
	}
	public TriggerInterval disable() {
		return (
			super.disable() == null
			? null
			: this
		);
	}
	public TriggerInterval enable(final boolean enabled) {
		return (
			super.enable(enabled) == null
			? null
			: this
		);
	}



	public TriggerInterval repeat() {
		return (
			super.repeat() == null
			? null
			: this
		);
	}
	public TriggerInterval noRepeat() {
		return (
			super.noRepeat() == null
			? null
			: this
		);
	}
	public TriggerInterval runOnce() {
		return (
			super.runOnce() == null
			? null
			: this
		);
	}
	public TriggerInterval repeat(final boolean repeating) {
		return (
			super.repeat(repeating) == null
			? null
			: this
		);
	}



}
