package foo.ree.android.helloworld;

public class Hello {
    public static void main(String[] args) {
        String s = args.length > 0 ? args[0] : "World";
        System.out.println("Hello " + s);
    }
}
