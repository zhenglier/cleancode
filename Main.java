import teaching.Args;

public class Main {
    public static void main(String[] args) {
        try {
            Args arg = new Args("l,p#,d*", args);
            boolean logging = arg.getBoolean('l');
            int port = arg.getInt('p');
            String directory = arg.getString('d');
            executeApplication(logging, port, directory);
        } catch (Exception e) {
            System.out.printf("Unexpected error: %s\n", e.getMessage());
        }
    }

    private static void executeApplication(boolean logging, int port, String directory) {
        System.out.println("Logging: " + logging);
        System.out.println("Port: " + port);
        System.out.println("Directory: " + directory);
    }
}
