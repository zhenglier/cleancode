package teaching;

public class ArgumentMarshalerFactory {
    public static ArgumentMarshaler getMarshaler(String elementTail) throws ArgsException {
        if (elementTail.equals("*")) {
            return new StringArgumentMarshaler();
        } else if (elementTail.equals("#")) {
            return new IntegerArgumentMarshaler();
        } else if (elementTail.length() == 0) {
            return new BooleanArgumentMarshaler();
        } else {
            throw new ArgsException(ArgsException.ErrorCode.INVALID_ARGUMENT_FORMAT);
        }
    }
} 