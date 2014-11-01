package node;

public class Operation {
	private String term;
	private Integer result;
	
	public Operation(String term, Integer result) {
		this.setTerm(term);
		this.setResult(result);
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
	
	public String toString() {
		return term + " = " + result + "\n";
	}
}
