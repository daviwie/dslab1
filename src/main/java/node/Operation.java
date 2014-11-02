package node;

public class Operation {
	private String term;
	private Integer result;

	public Operation(String term) {
		this.setTerm(term);
		this.calcResult();
	}

	public Integer getResult() {
		return result;
	}

	public void setResult(Integer result) {
		this.result = result;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	private void calcResult() {
		String[] parts = term.split(" ");

		Integer operand1 = Integer.parseInt(parts[0]);
		String operator = parts[1];
		Integer operand2 = Integer.parseInt(parts[2]);

		if (operator.equals("+")) {
			result = operand1 + operand2;
		}

		if (operator.equals("-")) {
			result = operand1 - operand2;
		}

		if (operator.equals("*")) {
			result = operand1 * operand2;
		}

		if (operator.equals("/")) {
			// TODO This might not work perfectly...
			Double one = Double.parseDouble(parts[0]);
			Double two = Double.parseDouble(parts[2]);
			result = (int)Math.ceil(one / two);
		}

	}

	public String toString() {
		return term + " = " + result + "\n";
	}
}
