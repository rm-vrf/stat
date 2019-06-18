//package cn.batchfile.stat.util.cmd;
//
//import java.io.InputStream;
//import java.lang.reflect.Field;
//
//import org.codehaus.plexus.util.cli.CommandLineException;
//import org.codehaus.plexus.util.cli.CommandLineUtils;
//import org.codehaus.plexus.util.cli.Commandline;
//import org.codehaus.plexus.util.cli.StreamConsumer;
//import org.codehaus.plexus.util.cli.StreamFeeder;
//import org.codehaus.plexus.util.cli.StreamPumper;
//
//public class CommandLineExecutor {
//
//	public static CommandLineCallable executeCommandLine(final Commandline cl, final InputStream systemIn,
//			final StreamConsumer systemOut, final StreamConsumer systemErr, final int timeoutInSeconds)
//					throws CommandLineException {
//
//		if (cl == null) {
//			throw new IllegalArgumentException("cl cannot be null.");
//		}
//
//		final Process p = cl.execute();
//
//		final StreamFeeder inputFeeder = systemIn != null ? new StreamFeeder(systemIn, p.getOutputStream()) : null;
//
//		final StreamPumper outputPumper = new StreamPumper(p.getInputStream(), systemOut);
//
//		final StreamPumper errorPumper = new StreamPumper(p.getErrorStream(), systemErr);
//
//		if (inputFeeder != null) {
//			inputFeeder.start();
//		}
//
//		outputPumper.start();
//
//		errorPumper.start();
//
//		final ProcessHook processHook = new ProcessHook(p);// 152
//
//		ShutdownHookUtils.addShutDownHook(processHook);
//
//		return new CommandLineCallable() {
//			public Integer call() throws CommandLineException {
//				try {
//					int returnValue;
//					if (timeoutInSeconds <= 0) {
//						returnValue = p.waitFor();
//					} else {
//						long now = System.currentTimeMillis();
//						long timeoutInMillis = 1000L * timeoutInSeconds;
//						long finish = now + timeoutInMillis;
//						while (CommandLineUtils.isAlive(p) && (System.currentTimeMillis() < finish)) {
//							Thread.sleep(10);
//						}
//
//						if (CommandLineUtils.isAlive(p)) {
//							throw new InterruptedException(
//									"Process timeout out after " + timeoutInSeconds + " seconds");
//						}
//
//						returnValue = p.exitValue();
//					}
//
//					waitForAllPumpers(inputFeeder, outputPumper, errorPumper);
//
//					/*if (outputPumper.getException() != null) {
//						throw new CommandLineException("Error inside systemOut parser", outputPumper.getException());
//					}
//
//					if (errorPumper.getException() != null) {
//						throw new CommandLineException("Error inside systemErr parser", errorPumper.getException());
//					}*/
//
//					return returnValue;
//				} catch (InterruptedException ex) {
//					/*if (inputFeeder != null) {
//						inputFeeder.disable();
//					}
//					outputPumper.disable();
//					errorPumper.disable();*/
//					throw new CommandLineException("Error while executing external command, process killed.", ex);
//				} finally {
//					ShutdownHookUtils.removeShutdownHook(processHook);
//
//					processHook.run();
//
//					if (inputFeeder != null) {
//						inputFeeder.close();
//					}
//
//					outputPumper.close();
//
//					errorPumper.close();
//				}
//			}
//
//			public void destroy() {
//				//logger.debug("stop tail process", "CommandLineCallable");
//				p.destroy();
//				if (CommandLineUtils.isAlive(p)) {
//					p.destroyForcibly();
//				}
//			}
//
//			public long getPid() {
//				try {
//					Object obj = getFieldValue(p, "pid");
//					return Long.valueOf(obj.toString());
//				} catch (NoSuchFieldException e) {
//					try {
//						Object obj = getFieldValue(p, "handle");
//						return Long.valueOf(obj.toString());
//					} catch (NoSuchFieldException ex) {
//						throw new RuntimeException("cannot get pid", ex);
//					}
//				}
//			}
//
//			private Object getFieldValue(Object obj, String fieldName) throws NoSuchFieldException {
//				try {
//					Field field = obj.getClass().getDeclaredField(fieldName);
//					field.setAccessible(true);
//					Object o = field.get(obj);
//					return o;
//				} catch (NoSuchFieldException e) {
//					throw e;
//				} catch (Exception e) {
//					return null;
//				}
//			}
//		};
//
//	}
//
//	private static void waitForAllPumpers(StreamFeeder inputFeeder, StreamPumper outputPumper, StreamPumper errorPumper)
//			throws InterruptedException {
//		/*if (inputFeeder != null) {
//			inputFeeder.waitUntilDone();
//		}
//
//		outputPumper.waitUntilDone();
//		errorPumper.waitUntilDone();*/
//	}
//
//}
