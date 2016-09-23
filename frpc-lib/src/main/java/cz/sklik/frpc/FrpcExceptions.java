package cz.sklik.frpc;

public class FrpcExceptions {

    static final String INT      = "int";

    static final String LONG     = "long";

    static final String DOUBLE   = "double";

    static final String BOOLEAN  = "boolean";

    static final String STRING   = "String";

    static final String STRUCT   = "FrpcStruct";

    static final String ARRAY    = "FrpcArray";

    static final String DATETIME = "DateTime";

    static final String BINARY   = "ByteBuffer";

    public static class FrpcElementException extends RuntimeException {

        private static final long serialVersionUID = 1L;

    }

    public static class NoElementWithName extends FrpcElementException {

        private static final long serialVersionUID = 1L;

        private final String      name;

        private final FrpcStruct  data;

        public NoElementWithName(String name, FrpcStruct data) {
            this.name = name;
            this.data = data;
        }

        public String toString() {
            return "No element with key " + this.name + "; possible keys " + this.data.getKeys();
        }
    }

    public static class NoElementOfTypeWithName extends FrpcElementException {

        private static final long serialVersionUID = 1L;

        private final String      key;

        private final Object      item;

        private final String      type;

        public NoElementOfTypeWithName(String key, Object item, String type) {
            this.key = key;
            this.item = item;
            this.type = type;
        }

        public String toString() {
            return String.format("Item %s is not %s; it is ", this.key, this.type, this.item
                    .getClass().toString());
        }
    }

    public static class NoElementOfTypeOnIndex extends FrpcElementException {

        private static final long serialVersionUID = 1L;

        private final int         index;

        private final Object      item;

        private final String      type;

        public NoElementOfTypeOnIndex(int index, Object item, String type) {
            this.index = index;
            this.item = item;
            this.type = type;
        }

        public String toString() {
            return String.format("Item on index %d is not %s; it is ", this.index, this.type,
                    this.item.getClass().toString());
        }
    }

    public static class ElementWithKeyNullException extends FrpcElementException {

        /**
         * 
         */
        private static final long serialVersionUID = -6299151357613876133L;

        private final String      key;

        private final String      type;

        public ElementWithKeyNullException(String key, String type) {
            this.key = key;
            this.type = type;
        }

        public String toString() {
            return String.format("Item %s is null, expected type is %s ", this.key, this.type);
        }

    }

    public static class ElementAtIndexNullException extends FrpcElementException {

        /**
         * 
         */
        private static final long serialVersionUID = -3043387187621934453L;

        private final int         index;

        private final String      type;

        public ElementAtIndexNullException(int index, String type) {
            this.index = index;
            this.type = type;
        }

        public String toString() {
            return String.format("Item at index %d is null, expected type is %s ", this.index,
                    this.type);
        }

    }
}
