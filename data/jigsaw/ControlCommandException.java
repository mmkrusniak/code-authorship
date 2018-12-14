// DirectoryResource.java
// $Id: ControlCommandException.java,v 1.2 2000/08/16 21:37:47 ylafon Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.ssi.commands ;

public class ControlCommandException extends Exception {

  public ControlCommandException(String msg) {
    super(msg);
  }

  public ControlCommandException(String name,String msg) {
    super("["+name+"] : "+msg);
  }

}
