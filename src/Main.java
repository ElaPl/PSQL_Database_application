
public class Main {
	public static void main(String args[]) {

		Mediator mediator = new Mediator();
		for (String s: args) {
            System.out.println(s);
            mediator.execute_file(s);
        }
	}
}