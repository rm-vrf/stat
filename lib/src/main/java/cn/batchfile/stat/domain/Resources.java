package cn.batchfile.stat.domain;

/**
 * 资源
 * @author lane.cn@gmail.com
 *
 */
public class Resources {

	private ControlGroup limits;
	private ControlGroup reservations;

	/**
	 * 资源限制
	 * @return 资源限制
	 */
	public ControlGroup getLimits() {
		return limits;
	}

	/**
	 * 资源限制
	 * @param limits 资源限制
	 */
	public void setLimits(ControlGroup limits) {
		this.limits = limits;
	}

	/**
	 * 资源保留
	 * @return 资源保留
	 */
	public ControlGroup getReservations() {
		return reservations;
	}

	/**
	 * 资源保留
	 * @param reservations 资源保留
	 */
	public void setReservations(ControlGroup reservations) {
		this.reservations = reservations;
	}

}
