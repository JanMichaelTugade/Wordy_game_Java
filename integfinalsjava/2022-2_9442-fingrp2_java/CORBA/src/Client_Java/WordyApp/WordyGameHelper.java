package Client_Java.WordyApp;


/**
* WordyApp/WordyGameHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from WordyApp.idl
* Monday, May 8, 2023 9:51:43 PM CST
*/

abstract public class WordyGameHelper
{
  private static String  _id = "IDL:WordyApp/WordyGame:1.0";

  public static void insert (org.omg.CORBA.Any a, WordyGame that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static WordyGame extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = org.omg.CORBA.ORB.init ().create_interface_tc (WordyGameHelper.id (), "WordyGame");
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static WordyGame read (org.omg.CORBA.portable.InputStream istream)
  {
    return narrow (istream.read_Object (_WordyGameStub.class));
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, WordyGame value)
  {
    ostream.write_Object ((org.omg.CORBA.Object) value);
  }

  public static WordyGame narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof WordyGame)
      return (WordyGame)obj;
    else if (!obj._is_a (id ()))
      throw new org.omg.CORBA.BAD_PARAM ();
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      _WordyGameStub stub = new _WordyGameStub();
      stub._set_delegate(delegate);
      return stub;
    }
  }

  public static WordyGame unchecked_narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof WordyGame)
      return (WordyGame)obj;
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      _WordyGameStub stub = new _WordyGameStub();
      stub._set_delegate(delegate);
      return stub;
    }
  }

}
