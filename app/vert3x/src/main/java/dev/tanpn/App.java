package dev.tanpn;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {
		System.out.println("Starting...");
		new MyLauncher().dispatch(new String[] {"run", VerticleInitializer.class.getName()});
	}
}
