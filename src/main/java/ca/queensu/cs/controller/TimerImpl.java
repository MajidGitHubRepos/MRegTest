package ca.queensu.cs.controller;

import java.math.BigDecimal;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.eclipse.microprofile.metrics.Snapshot;
import org.eclipse.microprofile.metrics.Timer;


public class TimerImpl implements Timer {
	private final double offset;
	
	public TimerImpl() {
        final int count = 500;
        BigDecimal offsetSum = BigDecimal.ZERO;
        for (int i = 0; i < count; i++) {
            offsetSum = offsetSum.add(BigDecimal.valueOf(calculateOffset()));
        }
        offset = (offsetSum.divide(BigDecimal.valueOf(count))).longValue();
    }
	
	 private static double calculateOffset() {
	        final double nano = System.nanoTime();
	        final double nanoFromMilli = System.currentTimeMillis() * 1_000_000;
	        return nanoFromMilli - nano;
	    }

	    

	    public double nowNano() {
	        return offset + System.nanoTime();
	    }

	    public double nowMicro() {
	        return (offset + System.nanoTime()) / 1000;
	    }

	    public double nowMilli() {
	        return System.currentTimeMillis();
	    }

		@Override
		public long getCount() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public double getFifteenMinuteRate() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public double getFiveMinuteRate() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public double getMeanRate() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public double getOneMinuteRate() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Snapshot getSnapshot() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Context time() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <T> T time(Callable<T> arg0) throws Exception {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void time(Runnable arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void update(long arg0, TimeUnit arg1) {
			// TODO Auto-generated method stub
			
		}

}
