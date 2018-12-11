public class DummyClass {
  String a;
  String b;
  String c;
  String d;
  String e;
  String f;
  String g;
  
  public void main(String[] args) {
    new DummyClass(1, "").doSomething(new Integer(9));
  }
  public DummyClass(int a, String b) {
    //do absolutely nothing
  }
  
  public void doSomething(int i) {
    System.out.println("Hello! Parameter = " + i);
  }
  @Override
  public DummyClass clone() throws CloneNotSupportedException {
    return new DummyClass(1, "");
  }
}

import java.awt.event.*;
import java.lang.reflect.Field;

import javax.swing.*;

public class FieldDialog extends JDialog implements ActionListener {
  private static final long serialVersionUID = 1L; //Eclipse requirement
  
  private JDisplayer source; //JDisplayer that this component comes from
  
  //Components
  private JList<NamedField> fieldList = new JList<NamedField>(); //List of fields for object
  private JButton get; //"Get" button
  private JButton inspect; //"Inspect" button
  private ObjectBox objectBox; //Object whose fields this dialog holds
  private JLabel output; //Displays result of "get" operation
  JLabel name;
  
  private ObjectIO objectIO;
  
  public FieldDialog(JDisplayer source) {
    super(source, "[JDisplayer] Inspect field...", true);
    this.source = source;
    this.setSize(300, 400);
    this.setModal(true);
    objectIO = new ObjectIO(source.bench);
    
    fieldList.setSize(200, 100);
    fieldList.setOpaque(true);
    fieldList.setVisible(true);
    
    JScrollBar scrollbar = new JScrollBar(SwingConstants.VERTICAL);
    scrollbar.setUnitIncrement(3);
    scrollbar.setVisible(true);
    
    JScrollPane pane = new JScrollPane();
    pane.setSize(200, 250);
    pane.setLocation(50, 40);
    pane.setVisible(true);
    pane.setVerticalScrollBar(scrollbar);
    pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    pane.setViewportView(fieldList);
    this.add(pane);
    
    
    name = new JLabel();
    name.setSize(200, 30);
    name.setLocation(50, 10);
    name.setVisible(true);
    this.add(name);
    System.out.println(name);
    
    get = new JButton("Get");
    get.setSize(90, 20);
    get.setLocation(50, 290);
    get.setVisible(true);
    get.addActionListener(this);
    this.add(get);
    
    inspect = new JButton("Inspect");
    inspect.setSize(90, 20);
    inspect.setLocation(160, 290);
    inspect.setVisible(true);
    inspect.addActionListener(this);
    this.add(inspect);
    
    output = new JLabel("");
    output.setSize(200, 30);
    output.setLocation(50, 310);
    output.setVisible(true);
    this.add(output);
  }
  
  public void OpenFieldDialog() {
    Field[] rawFields = objectBox.getObject().getClass().getDeclaredFields();
    NamedField[] fields = new NamedField[rawFields.length];
    for(int i = 0; i < rawFields.length; i++) fields[i] = new NamedField(rawFields[i]);
    fieldList.setListData(fields);
    
    name.setText("Fields of " + objectBox.getObject().getClass().getSimpleName() + " \"" + objectBox.getName() + "\"");
    output.setText("");
  
    this.setVisible(true);
  }

  //**************************************
  //Following are Listener methods.
  //**************************************
  @Override
  public void actionPerformed(ActionEvent e) {
    
    //Part 1: Get an object from the selected field
    Field field = fieldList.getSelectedValue().getField();
    if(field == null) return;
    Object result = null;
    
    try {
      result = field.get(objectBox.getObject());
    } catch (IllegalArgumentException e1) {
      doGetError("This object does not have that field");
      return;
    } catch (IllegalAccessException e1) {
      doGetError("Cannot access that field");
      return;
    } if(result == null) {
      doGetError("That field is null.");
      return;
    }
    
    
    if(e.getSource() == get) {
      
      
      System.out.println("Got a " + result.getClass().getSimpleName());
      if(objectIO.isWrapper(result.getClass())) {
        output.setText("Success! Field value = " + result);
      } else {
        output.setText("Success! Field sent to bench.");
        ObjectBox newBox = new ObjectBox(result, field.getName());
        source.bench.addObject(newBox);
        source.add(newBox);
        newBox.addMouseListener(source);
      }
    }
    
    if(e.getSource() == inspect) {
      FieldDialog subDialog = new FieldDialog(source);
      subDialog.setObject(new ObjectBox(result, field.getName()));
      subDialog.OpenFieldDialog();
    }
  }

  private void doGetError(String message) {
    output.setText(message);
  }
  
  public Object getObject() {
    return objectBox.getObject();
  }

  public void setObject(ObjectBox object) {
    this.objectBox = object;
  }
  
}

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.imageio.ImageIO;

public class JDisplayer extends JFrame implements ActionListener, MouseListener, WindowListener {
  //JFrame is serializable
  private static final long serialVersionUID = 1L;
  
  private static final int WIDTH = 500;
  private static final int HIEGHT = 500;
  
  private JMenuBar menuBar;
  private JMenuItem optionMenu;
  private JMenuItem objectMenu;
  private JMenuItem removeAllItem;
  private JMenuItem addItem;
  private JMenuItem importItem;
  
  private JButton addImport;
  private JButton removeImport;
  private JTextField importField;
  private JList<String> importsList; 
  
  private JButton addObject;
  protected ObjectBench bench = new ObjectBench();
  private ObjectDialog newObjectDialog;
  private MethodDialog newMethodDialog;
  private FieldDialog newFieldDialog;
  private JPanel trash;
  private JPopupMenu popup;
  private JObjectMenuItem removeObject = new JObjectMenuItem("Remove");
  private JMenu methodsMenu = new JMenu("Methods");
  private JObjectMenuItem fieldsMenu = new JObjectMenuItem("Fields");
  private JObjectMenuItem copyMenu = new JObjectMenuItem("Copy");
  
  @SuppressWarnings("unused")
  private ObjectIO objectIO;
  
  public static void main(String[] args){
    new JDisplayer();
  }
  
  public JDisplayer() {
    super("JDisplayer");
    
    this.getContentPane().setBackground(new Color(140, 90, 0));
    
    //Uncomment for system look and feel
    //Comment for java look and feel
    /*try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch(Exception e) {}*/
    
    this.setSize(WIDTH, HIEGHT);
    this.setLayout(null);
    this.setLocation(100, 100);
    this.addWindowListener(this); //Listener-ception
    this.addMouseListener(this);
    
    URL file = this.getClass().getResource("trash.png");
    Image image = null;
    
    trash = new JPanel();
    trash.setLocation(10, 10);
    trash.setSize(50, 50);
    
    try {
      image = ImageIO.read(file).getSubimage(56, 44, 196 - 56, 207).getScaledInstance(trash.getWidth() - 10, trash.getHeight(), BufferedImage.SCALE_SMOOTH);
    } catch (IOException e) {
      e.printStackTrace();
    }
    JLabel can = new JLabel(new ImageIcon(image));
    can.setSize(trash.getSize());
    trash.setOpaque(false);
    trash.add(can);
    //trash.setBackground(Color.BLACK);
    this.add(trash);
    
    //
    //Dialog init
    //
    newObjectDialog = new ObjectDialog(this);
    newObjectDialog.setSize(300, 200);
    newObjectDialog.setLocation(this.getLocation().x + 20, this.getLocation().y + 20);
    newObjectDialog.setLayout(null);
    
    newFieldDialog = new FieldDialog(this);
    newFieldDialog.setLocation(this.getLocation().x + 20, this.getLocation().y + 20);
    newFieldDialog.setLayout(null);
    
    newMethodDialog = new MethodDialog(this);
    newMethodDialog.setSize(300, 200);
    newMethodDialog.setLocation(this.getLocation().x + 20, this.getLocation().y + 20);
    newMethodDialog.setLayout(null);
    
    //
    //Button init
    //
    addObject = new JButton("Add Object...");
    addObject.setSize(WIDTH / 2, 20);
    addObject.setLocation(WIDTH / 4, 40);
    addObject.addActionListener(this);
    this.add(addObject);
    
    //
    //Menu init
    //
    menuBar = new JMenuBar();
    this.setJMenuBar(menuBar);
    
    //"Option" menu
    optionMenu = new JMenu("Option");
    menuBar.add(optionMenu);
    importItem = new JMenuItem("Standard packages...");
    importItem.addActionListener(this);
    optionMenu.add(importItem);
    
    //"Object" menu
    objectMenu = new JMenu("Object");
    menuBar.add(objectMenu);
    removeAllItem = new JMenuItem("Remove all [recompile]");
    removeAllItem.setForeground(new Color(200, 0, 0));
    removeAllItem.addActionListener(this);
    addItem = new JMenuItem("Add object...");
    addItem.setForeground(new Color(0, 150, 150));
    addItem.addActionListener(this);
    objectMenu.add(removeAllItem);
    objectMenu.add(addItem);
    
    removeObject.addActionListener(this);
    objectIO = new ObjectIO(bench);
    this.setVisible(true);
    ObjectBox defaultObject = new ObjectBox(new Integer(1), "default");
    defaultObject.addMouseListener(this);
    bench.addObject(defaultObject);
    updateDisplay();
  }

  public void updateDisplay() {
    Point mouseLocation = getMouseLocation();
    mouseLocation.x = mouseLocation.x - 5;
    mouseLocation.y = mouseLocation.y - 50;
    System.out.println(mouseLocation);
    LinkedList<ObjectBox> removables = new LinkedList<ObjectBox>();
    for(ObjectBox b: bench.getObjects()) {
      if(trash.contains(mouseLocation)) removables.add(b);
      if(b.isBeingDragged && this.contains(mouseLocation)) {
        b.setLocation(mouseLocation.x - 90, mouseLocation.y - 10);
      } else if(! this.contains(mouseLocation) && b.isBeingDragged) {
        b.setLocation(100, 100);
      }
      if(! this.isAncestorOf(b)) this.add(b);
    }
    
    for(ObjectBox b: removables) {
      this.remove(b);
      bench.removeObject(b);
    }
  }
  private Point getMouseLocation() {
    Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
    SwingUtilities.convertPointFromScreen(mouseLocation, this);
    return mouseLocation;
  }
  
  private void removeAllBoxes() {
    int size = bench.getObjects().size();
    for(int i = 0; i < size; i++) {
      this.remove(bench.getObjects().get(i));
      bench.getObjects().remove(i);
    }
    updateDisplay();
    paint(this.getGraphics());
  }
  
  /*
  private void paintBackground(Graphics g) {
    URL file = this.getClass().getResource("background.png");
    BufferedImage image;
    try {
      image = ImageIO.read(file);
      g.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), null);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  @Override
  public void paint(Graphics g) {
    for(Component c: this.getComponents()) c.paintAll(g.create(c.getX(), c.getY(), c.getWidth(), c.getHeight()));
    paintBackground(g);
  }
  
  @Override
  public void paintAll(Graphics g) {
    paintBackground(g);
    for(Component c: this.getComponents()) c.paintAll(g.create(c.getX(), c.getY(), c.getWidth(), c.getHeight()));
  }
  */
  //Listener methods
  boolean isDragging = false;
  @Override
  public void actionPerformed(ActionEvent event) {
    if(event.getSource() == addObject) {
      newObjectDialog.OpenObjectDialog();
      updateDisplay();
    }
    if(event.getSource() instanceof JMethodMenuItem) {
      JMethodMenuItem item = (JMethodMenuItem)event.getSource();
      newMethodDialog.setMethod(item.getMethod());
      newMethodDialog.setInvokee(item.getSource().getObject());
      newMethodDialog.OpenMethodDialog();
    }
    if(event.getSource() == removeObject) {
      System.out.println("Removing an objectbox...");
      bench.removeObject(removeObject.getObjectbox());
      this.remove(removeObject.getObjectbox());
      updateDisplay();
      paint(this.getGraphics());
    }
    if(event.getSource() == fieldsMenu) {
      System.out.println("Opening fields dialog...");
      newFieldDialog = new FieldDialog(this);
      newFieldDialog.setLocation(this.getLocation().x + 20, this.getLocation().y + 20);
      newFieldDialog.setLayout(null);
      newFieldDialog.setObject(fieldsMenu.getObjectbox());
      newFieldDialog.OpenFieldDialog();
    }
    if(event.getSource() == removeAllItem) removeAllBoxes();
    if(event.getSource() == copyMenu) {
      try {
        Method m = copyMenu.getObjectbox().getObject().getClass().getDeclaredMethod("clone");
        Object result = m.invoke(copyMenu.getObjectbox().getObject());
        ObjectBox box = new ObjectBox(result, copyMenu.getObjectbox().getName() + "Copy");
        bench.addObject(box);
        box.addMouseListener(this);
        this.add(box);
        this.paint(this.getGraphics());
        updateDisplay();
      } catch(Exception e) {e.printStackTrace();}
    }
    if(event.getSource() == importItem) {
      JDialog importsDialog = new JDialog(this, "[JDisplayer] Manage imports");
      
      String[] imports = new String[bench.getStandardImports().size()];
      for(int i = 0; i < bench.getStandardImports().size(); i++) imports[i] = (String) bench.getStandardImports().get(i);
      importsList = new JList<String>(imports);
      importsList.setSize(200, 100);
      importsList.setOpaque(true);
      importsList.setVisible(true);
      
      JScrollBar scrollbar = new JScrollBar(SwingConstants.VERTICAL);
      scrollbar.setUnitIncrement(3);
      scrollbar.setVisible(true);
      
      JScrollPane pane = new JScrollPane();
      pane.setSize(200, 250);
      pane.setLocation(45, 40);
      pane.setVisible(true);
      pane.setVerticalScrollBar(scrollbar);
      pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      pane.setViewportView(importsList);
      importsDialog.add(pane);
      
      JLabel label = new JLabel("Current imports:");
      label.setSize(200, 25);
      label.setLocation(45, 15);
      importsDialog.add(label);
      
      importField = new JTextField();
      importField.setSize(200, 25);
      importField.setLocation(45, 300);
      importsDialog.add(importField);
      
      addImport = new JButton("Add");
      addImport.setSize(90, 20);
      addImport.setLocation(45, 330);
      addImport.addActionListener(this);
      importsDialog.add(addImport);
      
      removeImport = new JButton("Remove");
      removeImport.setSize(90, 20);
      removeImport.setLocation(155, 330);
      removeImport.addActionListener(this);
      importsDialog.add(removeImport);
      
      importsDialog.setModal(true);
      importsDialog.setSize(300, 400);
      importsDialog.setLayout(null);
      importsDialog.setLocation(this.getX() + 100, this.getY() + 100);
      importsDialog.setVisible(true);
    }
    if(event.getSource() == addImport) {
      String importName = importField.getText();
      if(importName.equals("")) return;
      if(bench.hasStandardImport(importName)) return;
      bench.addStandardImport(importName);
      String[] imports = new String[bench.getStandardImports().size()];
      for(int i = 0; i < imports.length; i++) {
        imports[i] = bench.getStandardImports().get(i);
      }
      importsList.setListData(imports);
    }
    if(event.getSource() == removeImport) {
      String importName = importsList.getSelectedValue();
      if(importName.equals("")) return;
      bench.removeStandardImport(importName);
      String[] imports = new String[bench.getStandardImports().size()];
      for(int i = 0; i < imports.length; i++) {
        imports[i] = bench.getStandardImports().get(i);
      }
      importsList.setListData(imports);
    }
  }
  @Override
  public void mouseClicked(MouseEvent event) {
    
  }
  @Override
  public void mouseEntered(MouseEvent event) {
    
  }
  @Override
  public void mouseExited(MouseEvent event) {
    
  }
  @Override
  public void mousePressed(MouseEvent event) {
    if(event.getButton() == MouseEvent.BUTTON1) {
      System.out.print("Left click on ");
      System.out.println(event.getComponent());
      ObjectBox clickedOn = null;
      for(ObjectBox o: bench.getObjects()) if(o == event.getComponent()) clickedOn = o;
      if(clickedOn == null) return;

      clickedOn.isBeingDragged = true;
      this.setCursor(new Cursor(Cursor.MOVE_CURSOR));
    }
    
    if(event.getButton() == MouseEvent.BUTTON3 && event.getComponent() instanceof ObjectBox) {
      ObjectBox box = (ObjectBox) event.getComponent();
      Method[] methods = box.getObject().getClass().getMethods();
      ArrayList<JMenuItem> items = new ArrayList<JMenuItem>();
      
      
      System.out.print("Right click on ");
      System.out.println(event.getComponent());
      popup = new JPopupMenu();
      popup.setInvoker(event.getComponent());
      popup.show(this, getMouseLocation().x, getMouseLocation().y);
      
      for(int i = 0; i < methods.length; i++) {
        JMenu menu = null;
        for(int j = 0; j < items.size() && items.get(j) != null; j++) {
          String currentGroup = items.get(j).getName();
          String thisMethodClass = methods[i].getDeclaringClass().getName();
          if(currentGroup.equals(thisMethodClass)) menu = (JMenu) items.get(j);
        }
        if(menu == null) {
          
          menu = new JMenu();
          menu.setText(methods[i].getDeclaringClass().getName());
          menu.setName(methods[i].getDeclaringClass().getName());
          menu.setVisible(true);
          menu.setSize(200, 20);
          items.add(menu);
        }
        
        //Set up method item with proper name
        JMethodMenuItem item = new JMethodMenuItem(methods[i], box);
        item.setMethod(methods[i]);
        String descriptor = Modifier.toString(methods[i].getModifiers()) + " ";
        descriptor += methods[i].getReturnType().getSimpleName() + " ";
        descriptor += methods[i].getName() + "(";
        for(int j = 0; j < methods[i].getParameterTypes().length; j++) {
          Class<?> paramClass = methods[i].getParameterTypes()[j];
          descriptor += paramClass.getSimpleName();
          if(j != methods[i].getParameterTypes().length - 1) descriptor += ", ";
        }
        descriptor += ")";
        item.setText(descriptor);
        item.setOpaque(true);
        if(Modifier.isPublic(methods[i].getModifiers())) item.setBackground(new Color(255, 255, 220));
        if(Modifier.isStatic(methods[i].getModifiers())) item.setBackground(new Color(200, 220, 255));
        if(Modifier.isPrivate(methods[i].getModifiers())) item.setBackground(new Color(255, 100, 100));
        if(Modifier.isNative(methods[i].getModifiers())) item.setForeground(new Color(100, 0, 100));
        
        item.addActionListener(this);
        item.setSize(200, 20);
        menu.add(item);
      }
      
      removeObject.setObjectbox(box);
      removeObject.setForeground(new Color(200, 0, 0));
      popup.add(removeObject);
      
      methodsMenu = new JMenu("Methods");
      methodsMenu.setForeground(new Color(0, 0, 200));
      popup.add(methodsMenu);
      
      fieldsMenu = new JObjectMenuItem("Fields...");
      fieldsMenu.setObjectbox(box);
      fieldsMenu.setForeground(new Color(0, 200, 0));
      fieldsMenu.addActionListener(this);
      popup.add(fieldsMenu);
      
      try {
        box.getObject().getClass().getDeclaredMethod("clone");
        copyMenu = new JObjectMenuItem("Clone");
        copyMenu.setObjectbox(box);
        copyMenu.setForeground(new Color(150, 0, 150));
        copyMenu.addActionListener(this);
        popup.add(copyMenu);
      } catch(Exception e) {}
      
      for(JMenuItem j: items) methodsMenu.add(j);
      popup.setPopupSize(100, 80);
    }
  }
  @Override
  public void mouseReleased(MouseEvent event) {
    if(! (event.getComponent() instanceof ObjectBox)) return;
    updateDisplay();
    
    System.out.println("Unclick");
    for(ObjectBox o: bench.getObjects()) {
      o.isBeingDragged = false;
    }
    this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    
    updateDisplay();
  }
  @Override
  public void windowActivated(WindowEvent event) {}
  @Override
  public void windowClosed(WindowEvent event) {}
  @Override
  public void windowClosing(WindowEvent event) {
    System.exit(0);
  }
  @Override
  public void windowDeactivated(WindowEvent event) {}
  @Override
  public void windowDeiconified(WindowEvent event) {}
  @Override
  public void windowIconified(WindowEvent event) {}
  @Override
  public void windowOpened(WindowEvent event) {}
}

import java.lang.reflect.Method;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenuItem;

public class JMethodMenuItem extends JMenuItem {
  //Keep Eclipse from being annoying
  private static final long serialVersionUID = 1L;
  
  private Method method;
  private ObjectBox source;
  
  public JMethodMenuItem(Method m, ObjectBox source) {
    method = m;
    this.source = source;
  }

  public JMethodMenuItem() {
    super();
  }

  public JMethodMenuItem(Action arg0) {
    super(arg0);
  }

  public JMethodMenuItem(Icon arg0) {
    super(arg0);
  }

  public JMethodMenuItem(String arg0, Icon arg1) {
    super(arg0, arg1);
  }

  public JMethodMenuItem(String arg0, int arg1) {
    super(arg0, arg1);
  }

  public JMethodMenuItem(String arg0) {
    super(arg0);
  }

  public Method getMethod() {
    return method;
  }

  public void setMethod(Method method) {
    this.method = method;
  }

  public ObjectBox getSource() {
    return source;
  }

  public void setSource(ObjectBox source) {
    this.source = source;
  }
}

import javax.swing.JMenuItem;

public class JObjectMenuItem extends JMenuItem {
  private ObjectBox objectbox;
  public JObjectMenuItem(String name) {
    super(name);
  }
  public ObjectBox getObjectbox() {
    return objectbox;
  }
  public void setObjectbox(ObjectBox objectbox) {
    this.objectbox = objectbox;
  }
}

import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.*;

public class MethodDialog extends JDialog implements ActionListener {

  private static final long serialVersionUID = 1L;
  private JTextField methodDialogParameters;
  private JButton methodDialogGo;
  private JLabel methodDialogOutput;
  private JLabel parametersLabel;
  JTextArea label;
  
  private JDisplayer source;
  private ObjectBench bench;
  private Method method;
  private Object invokee;
  private ObjectIO objectIO;
  
  public MethodDialog(JDisplayer source) {
    super(source, "[JDisplayer] Invoke method...");
    this.setVisible(false);
    this.source = source;
    this.bench = source.bench;
    objectIO = new ObjectIO(bench);
    
    label = new JTextArea("");
    label.setSize(200, 50);
    label.setLocation(50, 10);
    label.setOpaque(false);
    label.setEditable(false);
    this.add(label);
    
    methodDialogOutput = new JLabel();
    methodDialogParameters = new JTextField();
    methodDialogGo = new JButton();
    parametersLabel = new JLabel("Enter parameters:");
    
    methodDialogOutput.setText("");
    methodDialogOutput.setSize(280, 20);
    methodDialogOutput.setLocation(10, 135);
    this.add(methodDialogOutput);
    
    parametersLabel.setSize(200, 20);
    parametersLabel.setLocation(50, 60);
    this.add(parametersLabel);
    
    methodDialogParameters.setText("");
    methodDialogParameters.setSize(200, 20);
    methodDialogParameters.setLocation(50, 80);
    this.add(methodDialogParameters);
    
    methodDialogGo.setText("Execute");
    methodDialogGo.setSize(180, 20);
    methodDialogGo.setLocation(60, 110);
    methodDialogGo.addActionListener(this);
    this.add(methodDialogGo);
    
    try {
      setMethod(Object.class.getDeclaredMethod("getClass")); //default method is methodception
    } catch (NoSuchMethodException | SecurityException e) {
      //this will not happen
      e.printStackTrace();
    }
    this.setModal(true);
  }
  
  public void OpenMethodDialog() {
    methodDialogOutput.setText("");
    this.setVisible(true);
  }
  
  public void setMethod(Method m) {
    System.out.println(m);
    this.method = m;
    String labelText = "Method ";
    labelText += m.getName() + "()\n";
    labelText += "Parameters: ";
    if(m.getParameterCount() == 0) {
      labelText += "None";
      methodDialogParameters.setVisible(false);
      parametersLabel.setText("No parameters.");
    } else {
      methodDialogParameters.setVisible(true);
      parametersLabel.setText("Enter parameters:");
    }
    
    for(int i = 0; i < m.getParameterCount(); i++) {
      Class<?> c = m.getParameterTypes()[i];
      labelText += c.getSimpleName();
      if(i != m.getParameterCount() - 1) labelText += ", ";
    }
    labelText += "\n";
    labelText += "Returns: " + m.getReturnType().getSimpleName();
    label.setText(labelText);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if(e.getSource() == methodDialogGo) {
      Object result = null;
      try {
        String parameters = (methodDialogParameters.getText());
        if(! parameters.equals("")) {
          result = method.invoke(invokee, objectIO.getObjectsFromParameters(parameters).toArray());
        } else result = method.invoke(invokee);
      } catch (IllegalAccessException e1) {
        doInvocationError("Cannot access method " + method.getName());
        return;
      } catch (IllegalArgumentException e1) {
        doInvocationError("Illegal arguments for method " + method.getName());
        return;
      } catch (InvocationTargetException e1) {
        doInvocationError(e1.getCause().getClass().getSimpleName() + " thrown by invokee");
        return;
      } catch (ClassNotFoundException e1) {
        doInvocationError("Could not find class " +  method.getClass().getName());
        return;
      } catch (InstantiationException e1) {
        doInvocationError("Could not instantiate parameters");
        return;
      } catch (NoSuchMethodException e1) {
        doInvocationError("No such method");
        return;
      } catch (SecurityException e1) {
        doInvocationError("Blocked by security");
        return;
      }
      System.out.println("Result: " + result.getClass() + "  " + result.toString());
      if(objectIO.isWrapper(result)) methodDialogOutput.setText("Sucess! Returned: " + result);
      else {
        ObjectBox resultObject = new ObjectBox(result, "Result");
        resultObject.addMouseListener(source);
        bench.addObject(resultObject);
        source.updateDisplay();
        methodDialogOutput.setText("Sucess! Result is on the bench.");
      }
    }
  }
  
  private void doInvocationError(String message) {
    methodDialogOutput.setText(message);
  }
  public void setInvokee(Object invokee) {
    this.invokee = invokee;
  }
}

import java.lang.reflect.*;

public class NamedField {
  private Field field;
  
  public NamedField(Field f) {
    field = f;
  }
  
  @Override
  public String toString() {
    String currentName = field.toString();
    String result = "";
    
    if(Modifier.isPrivate(field.getModifiers())) result += "private ";
    if(Modifier.isProtected(field.getModifiers())) result += "protected ";
    if(Modifier.isPublic(field.getModifiers())) result += "public ";
    if(Modifier.isVolatile(field.getModifiers())) result += "volatile ";
    if(Modifier.isStatic(field.getModifiers())) result += "static ";
    if(Modifier.isFinal(field.getModifiers())) result += "final ";
    
    result += field.getType().getSimpleName() + " ";
      
    int i = 0;
    while(i != -1) {
      i = currentName.indexOf(' ');
      currentName = currentName.substring(i + 1);
    }
    i = 0;
    while(i != -1) {
      i = currentName.indexOf('.');
      currentName = currentName.substring(i + 1);
    }
    result += currentName;
    return result;
  }
  
  public String getName() {
    String currentName = field.toString();
    int i = 0;
    while(i != -1) {
      i = currentName.indexOf(' ');
      currentName = currentName.substring(i + 1);
    }
    i = 0;
    while(i != -1) {
      i = currentName.indexOf('.');
      currentName = currentName.substring(i + 1);
    }
    return currentName;
  }

  public Field getField() {
    return field;
  }

  public void setField(Field field) {
    this.field = field;
  }
}

import java.lang.reflect.*;
import java.util.LinkedList;

public class ObjectBench {
  private LinkedList<ObjectBox> objects;
  private LinkedList<String> standardImports = new LinkedList<String>();
  
  /*instance*/{
    standardImports.add("java.lang");
    standardImports.add(this.getClass().getPackage().getName());
  }
  
  public static void main(String[] args) {
    DummyClass d = new DummyClass(1, "");
    doMethod(d, "com.miolean.tests.DummyClass.doSomething()", 18);
  }
  
  public ObjectBench() {
    objects = new LinkedList<ObjectBox>();
  }
  
  void addObject(ObjectBox o) {
    objects.add(o);
  }
  
  void removeObject(ObjectBox o) {
    objects.remove(o);
  }
  
  private static boolean doMethod(Object invokee, String methodName, Object... parameters) {
    if(methodName.contains("()")) methodName = methodName.substring(0, methodName.length() - 2);
    
    Class<?>[] classes = new Class<?>[parameters.length];
    
    for(int j = 0; j < parameters.length; j++){
      //Unfold primitives, hooray!
      if(parameters[j] instanceof Integer) classes[j] = Integer.TYPE;
      else if(parameters[j] instanceof Boolean) classes[j] = Boolean.TYPE;
      else if(parameters[j] instanceof Short) classes[j] = Short.TYPE;
      else if(parameters[j] instanceof Long) classes[j] = Long.TYPE;
      else if(parameters[j] instanceof Character) classes[j] = Character.TYPE;
      else if(parameters[j] instanceof Float) classes[j] = Float.TYPE;
      else if(parameters[j] instanceof Double) classes[j] = Double.TYPE;
      else if(parameters[j] instanceof Byte) classes[j] = Byte.TYPE;
      else classes[j] = parameters[j].getClass();
    }
    
    //Find the '.' between the class and the method names
    int i = methodName.length() - 1;
    for( ; methodName.charAt(i) != '.' && i > 0; i--) ;
    
    try {
      Class<?> c = Class.forName(methodName.substring(0, i));
      
      System.out.println(c.getName());
      Method[] methods = c.getMethods();
      for(Method m: methods) System.out.println(m.getName());
        
      Method method = c.getMethod(methodName.substring(i + 1), classes);
      method.invoke(invokee, parameters);
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  public LinkedList<ObjectBox> getObjects() {
    return objects;
  }

  public void setObjects(LinkedList<ObjectBox> objects) {
    this.objects = objects;
  }

  public LinkedList<String> getStandardImports() {
    return standardImports;
  }

  public void addStandardImport(String import_) {
    this.standardImports.add(import_);
  }
  
  public void removeStandardImport(String import_) {
    int size = standardImports.size();
    for(int i = 0; i < size; i++) if(standardImports.get(i).equals(import_)) standardImports.remove(i);
  }
  
  public boolean hasStandardImport(String import_) {
    int size = standardImports.size();
    for(int i = 0; i < size; i++) if(standardImports.get(i).equals(import_)) return true;
    return false;
  }
}

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.lang.reflect.*;
import javax.swing.*;

public class ObjectBox extends JPanel {
  //Necessary to stop Eclipse from jabbering but not really supported
  private static final long serialVersionUID = 1L;
  
  private Object object;
  private String name;
  @Deprecated
  private Rectangle representation;
  private String parameters;
  private Color boxColor = new Color(50, 150, 255);
  private Color textColor = Color.BLACK;
  private Color dragBoxColor = Color.YELLOW;
  
  public boolean isBeingDragged = false;
  
  public ObjectBox(Object object, String name) {
    this.object = object;
    this.name = name;
    this.setSize(100, 100);
    this.setLocation(100, 100);
    this.setVisible(true);
    this.setSize(100, 50);
  }
  
  public Method[] getMethods() {
    return this.getClass().getMethods();
  }
  
  @Override
  public String toString() {
    String result = "";
    result += "ObjectBox containing a " + ((object == null)? "null object " :(object.getClass().getName() + "-type object ")) + "called " + name;
    return result;
  }
  
  @Override
  public void paintComponent(Graphics g) {
    this.setBackground(boxColor);
    
    super.paintComponent(g);
    
    g.setColor(dragBoxColor);
    g.fillRect(this.getWidth() - 21, 0, 20, 20);
    
    g.setColor(textColor);
    g.drawLine(0, 0, this.getWidth() - 1, 0);
    g.drawLine(0, 0, 0, this.getHeight() - 1);
    g.drawLine(this.getWidth() - 1, 0, this.getWidth() - 1, this.getHeight() - 1);
    g.drawLine(0, this.getHeight() - 1, this.getWidth() - 1, this.getHeight() - 1);
    
    g.drawString(name, 3, 15);
    g.drawString("{" + object.getClass().getSimpleName() + "}", 3, 35);
  }
  
  public Object getObject() {
    return object;
  }
  public void setObject(Object object) {
    this.object = object;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Deprecated
  public Rectangle getRepresentation() {
    return representation;
  }

  @Deprecated
  public void setRepresentation(Rectangle representation) {
    this.representation = representation;
    this.setLocation(representation.x, representation.y);
    this.setSize(representation.width, representation.height);
  }

  public String getParameters() {
    return parameters;
  }

  public void setParameters(String parameters) {
    this.parameters = parameters;
  }
}

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import javax.swing.*;

public class ObjectDialog extends JDialog implements ActionListener {
  
  private static final long serialVersionUID = 1L;
  
  private JDisplayer source;
  
  private static int newNameNumber = 1;
  private JTextField objectDialogName;
  private JTextField objectDialogParameters;
  private JButton objectDialogGo;
  private JLabel objectDialogOutput;
  private ObjectIO objectIO;
  private JLabel classNamelabel;
  private JLabel parametersLabel;
  
  public ObjectDialog(JDisplayer source) {
    super(source, "[JDisplayer] Instantiate object...");
    this.setVisible(false);
    this.source = source;
    objectIO = new ObjectIO(source.bench);
    this.setModal(true);
    
    classNamelabel = new JLabel("New object name: ");
    classNamelabel.setSize(200, 20);
    classNamelabel.setLocation(50, 10);
    this.add(classNamelabel);
    
    objectDialogOutput = new JLabel("");
    objectDialogOutput.setSize(280, 20);
    objectDialogOutput.setLocation(10, 135);
    this.add(objectDialogOutput);
    
    objectDialogName = new JTextField();
    objectDialogName.setSize(200, 20);
    objectDialogName.setLocation(50, 30);
    this.add(objectDialogName);
    
    parametersLabel = new JLabel("Initializer [ i.e. new Integer(1) ]");
    parametersLabel.setSize(200, 20);
    parametersLabel.setLocation(50, 60);
    this.add(parametersLabel);
    
    objectDialogParameters = new JTextField();
    objectDialogParameters.setSize(200, 20);
    objectDialogParameters.setLocation(50, 80);
    this.add(objectDialogParameters);
    
    objectDialogGo = new JButton("Create");
    objectDialogGo.setSize(180, 20);
    objectDialogGo.setLocation(60, 110);
    objectDialogGo.addActionListener(this);
    this.add(objectDialogGo);
    
  }
  
  public void OpenObjectDialog() {
    objectDialogOutput.setText("");
    
    this.setVisible(true);
  }

  //**************************************
  //Following are Listener methods.
  //**************************************
  @Override
  public void actionPerformed(ActionEvent e) {
    if(e.getSource() == objectDialogGo) {
      //Object makin' time
      String objectName = "";
      if(objectDialogName.getText().equals("")) {
        objectName = "newObject" + newNameNumber;
        newNameNumber++;
      } else objectName = objectDialogName.getText();
      
      for(int i = 0; i < objectName.length(); i++) {
        char c = objectName.charAt(i);
        if((!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c == '_'))) || objectName.charAt(0) == '_') {
          objectDialogOutput.setText("Not a valid Java identifier.");
          return;
        }
      }
      
      String params = objectDialogParameters.getText();
      //Get rid of that stupid semicolon that crashes JDisplayer
      if(params.charAt(params.length() - 1) == ';') params = params.substring(0, params.length() - 1);
      //add new if it isn't there already
      if(params.substring(0, 4).equals("new ")) params = "new " + params.substring(4);
      
      //Parse the class name
      String className = "?";
      try {
        className = objectIO.parseClass(params).getSimpleName();
      } catch (ClassNotFoundException e2) {
        doInitializationError("Could not find that class.");
      }
      ObjectBox objectBox = null;
      
      if(params.length() < 7 || !(params.contains("(") && params.contains(")"))) {
        doInitializationError("Could not parse parameters.");
        return;
      }
      
      //Make a new object with ObjectIO - what could go wrong?
      try {
        objectBox = new ObjectBox(objectIO.makeObject(params), objectName);
      } catch (ClassNotFoundException e1) {
        doInitializationError("Could not find class " + className);
        return;
      } catch (InstantiationException e1) {
        doInitializationError("Cannot instantiate " + className);
        return;
      } catch (IllegalAccessException e1) {
        doInitializationError("Cannot access " + className);
        return;
      } catch (NoSuchMethodException e1) {
        doInitializationError("No such constructor in " + className);
        return;
      } catch (SecurityException e1) {
        doInitializationError("Security blocked creation of new " + className);
        return;
      } catch (IllegalArgumentException e1) {
        doInitializationError("Illegal arguments");
        return;
      } catch (InvocationTargetException e1) {
        doInitializationError(e1.getTargetException() + " thrown in constructor");
        return;
      }
      
      source.bench.addObject(objectBox);
      System.out.println(objectBox);
      objectDialogOutput.setText("Object created.");
      objectBox.addMouseListener(source);
      objectDialogName.setText("");
      objectDialogParameters.setText("");
      
      source.paint(source.getGraphics());
      source.updateDisplay();
    }
  }
  
  private void doInitializationError(String message) {
    objectDialogOutput.setText(message);
  }
}

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ObjectIO {
  
  private ObjectBench bench;
  public ObjectIO(ObjectBench bench) {
    this.bench = bench;
  }
  //**************************************
  //Following are methods to
  //get / create Objects from Strings
  //based on Java syntax.
  //**************************************
  public LinkedList<Object> getObjectsFromParameters(String parameters) 
      throws ClassNotFoundException, 
      InstantiationException, 
      IllegalAccessException, 
      NoSuchMethodException, 
      SecurityException, 
      IllegalArgumentException, 
      InvocationTargetException {
    
    System.out.println("Getting objects from parameters " + parameters);
    LinkedList<Object> result = new LinkedList<Object>();
    
    Scanner scan = new Scanner(parameters);
    scan.useDelimiter(",");
    
    if(parameters.equals("")) {
      scan.close();
      return null;
    }
    
    while(scan.hasNext()) {
      String param = scan.next();
      while(param.charAt(0) == ' ') param = param.substring(1, param.length());
      try {
        while((param.length() - param.replace("\"", "").length()) % 2 == 1) param += scan.next();
      } catch(NoSuchElementException e) {
        scan.close();
        throw e;
      }
      Object o = makeObject(param);
      result.add(o);
      System.out.println("Made object: " + o);
    }
    
    scan.close();
    return result;
  }
  
  //Make a single object from the parameters in format ObjectType(newParam() or otherParam...) 
  public Object makeObject(String param) 
      throws ClassNotFoundException, 
      InstantiationException, 
      IllegalAccessException, 
      NoSuchMethodException, 
      SecurityException, 
      IllegalArgumentException, 
      InvocationTargetException {
    
    System.out.println("Param = " + param);
    if(param.equals("")) {
      throw new IllegalArgumentException();
    }
    
    if(param.length() > 4 && param.substring(0,4).equals("new ")) {
      //User wants to instantiate a new object as one of the parameters
      param = param.substring(4); //Out with the "new"
      
      int i; //defined as position of the first "(" in this initialization
      for(i = 0; param.charAt(i) != '('; i++) ; //Find the "("
      Class<?> newObjectClass = parseClass(param);
      
      //Figure out the type and the parameters based on Java syntax
      //Indirect recursion here
      LinkedList<Object> subparameters = getObjectsFromParameters(param.substring(i + 1, param.length() - 1));
      
      
      //The new object may have no parameters at all
      //In that case we don't have to muddle around with constructors
      if(subparameters == null) {
        return newObjectClass.newInstance();
      }
      
      //Figure out the classes of all of our parameters so we can get a constructor
      //Dealing with the primitives is kind of important
      Class<?>[] subParamClasses = null;
      subParamClasses = new Class<?>[subparameters.size()];
      for(int j = 0; j < subparameters.size() && subparameters.get(j) != null; j++) {
        if(subparameters.get(j) instanceof Integer) subParamClasses[j] = Integer.TYPE;
        else if(subparameters.get(j) instanceof Boolean) subParamClasses[j] = Boolean.TYPE;
        else if(subparameters.get(j) instanceof Short) subParamClasses[j] = Short.TYPE;
        else if(subparameters.get(j) instanceof Long) subParamClasses[j] = Long.TYPE;
        else if(subparameters.get(j) instanceof Character) subParamClasses[j] = Character.TYPE;
        else if(subparameters.get(j) instanceof Float) subParamClasses[j] = Float.TYPE;
        else if(subparameters.get(j) instanceof Double) subParamClasses[j] = Double.TYPE;
        else if(subparameters.get(j) instanceof Byte) subParamClasses[j] = Byte.TYPE;
        else subParamClasses[j] = subparameters.get(j).getClass();
      }
      
      //Try to get a constructor based on those classes
      Constructor<?> constructor = null;
      System.out.print("New constructor with parameters ");
      for(Class<?> s: subParamClasses) System.out.print(s.getCanonicalName() + " ");
      System.out.println(" (" + subParamClasses.length + " parameters)");
      constructor = newObjectClass.getConstructor(subParamClasses);
    
      
      //Create a new object using the constructor we just made
      System.out.println("Starting construction process with parameters " + subparameters);
      return constructor.newInstance(subparameters.toArray());
    } else return getOtherType(param);
  }
  
  public Object getOtherType(String param) {
    System.out.println("Found " + param + " as other type, boxing or formatting...");
    
    //First possibility: null
    //Easy enough
    if(param.equals("null") ) return null;
    //Second possibility: String
    //Empty strings are dealt with separately
    //Otherwise remove the quotes and we're good to go!
    if(param.contains("\"")) {
      if(param.length() == 2) return "";
      else return param.substring(1, param.length() - 1);
    }
    
    //Third possibility: Already-named object
    //Iterate through the objects that we have created to see if it's in there
    for(ObjectBox o: bench.getObjects()) if(param.equals(o.getName())) return o.getObject();
    
    //Fourth possibility: Primitive
    //We can use some tricks to figure out which is which
    if(param.equals("true")) return Boolean.valueOf(true);
    if(param.equals("false")) return Boolean.valueOf(false);
    if(param.charAt(0) == '\'') return new Character(param.charAt(1));
    
    //Any type beyond here is a numerical type, sometimes designated with a letter at the end
    if(param.contains(".") && (param.contains("f") || param.contains("F"))) return new Float(Float.parseFloat(param));
    if(param.contains(".")) return new Double(Double.parseDouble(param));
    if(param.contains("l") || param.contains("L")) return new Long(Long.parseLong(param));
    return new Integer(Integer.parseInt(param));
    //Short and byte are indistinguishable from int, so
    //they are not supported
    //Fortunately they are uncommon.
  }
  
  public Class<?> parseClass(String param) 
      throws ClassNotFoundException {
    
    int i; //defined as position of the "(" in this initialization
    for(i = 0; param.charAt(i) != '('; i++) ; //Find the "("
    String type = param.substring(0, i);
    
    if(type.contains("new ")) type = type.substring(4);
    
    //Try to find the type of the object - this is considered part of its parameters
    //First, try it as if the full package name was provided
    Class<?> newObjectClass = null;
    
    boolean classNotFound = true;
    try {
      newObjectClass = Class.forName(type);
      classNotFound = false;
    } catch (ClassNotFoundException e) {
      classNotFound = true;
    }
    //If that didn't work, iterate through the standard packages.
    int k = 0;
    while(classNotFound && k < bench.getStandardImports().size()) {
      String newType = "";
      try {
        newType = bench.getStandardImports().get(k) + "." + type;
        newObjectClass = Class.forName(newType);
        classNotFound = false;
      } catch (ClassNotFoundException e) {
        classNotFound = true;
      }
      k++;
    }
    //If full package name was specified the type will have had 1+ "."s in it
    if(classNotFound) {
      //Packaged but package or type is wrong (we would have found it otherwise)
      throw new ClassNotFoundException();
    }
    
    return newObjectClass;
  }
  
  public boolean doMethod(Object invokee, String methodName, Object... parameters) 
      throws IllegalAccessException, 
      IllegalArgumentException, 
      InvocationTargetException, 
      NoSuchMethodException, 
      SecurityException, 
      ClassNotFoundException {
    
    if(methodName.contains("()")) methodName = methodName.substring(0, methodName.length() - 2);
    
    Class<?>[] classes = new Class<?>[parameters.length];
    
    for(int j = 0; j < parameters.length; j++){
      //Unfold primitives, hooray!
      if(parameters[j] instanceof Integer) classes[j] = Integer.TYPE;
      else if(parameters[j] instanceof Boolean) classes[j] = Boolean.TYPE;
      else if(parameters[j] instanceof Short) classes[j] = Short.TYPE;
      else if(parameters[j] instanceof Long) classes[j] = Long.TYPE;
      else if(parameters[j] instanceof Character) classes[j] = Character.TYPE;
      else if(parameters[j] instanceof Float) classes[j] = Float.TYPE;
      else if(parameters[j] instanceof Double) classes[j] = Double.TYPE;
      else if(parameters[j] instanceof Byte) classes[j] = Byte.TYPE;
      else classes[j] = parameters[j].getClass();
    }
    
    //Find the '.' between the class and the method names
    int i = methodName.length() - 1;
    for( ; methodName.charAt(i) != '.' && i > 0; i--) ;
    
    Class<?> c = Class.forName(methodName.substring(0, i));
    
    System.out.println(c.getName());
    Method[] methods = c.getMethods();
    for(Method m: methods) System.out.println(m.getName());
      
    Method method = c.getMethod(methodName.substring(i + 1), classes);
    method.invoke(invokee, parameters);
  
    return true;
  }
  
  public boolean isWrapper(Object o) {
    return isWrapper(o.getClass());
  }
  public boolean isWrapper(Class<?> c) {
    if(c == Integer.class) return true;
    if(c == Short.class) return true;
    if(c == Long.class) return true;
    if(c == Byte.class) return true;
    if(c == Double.class) return true;
    if(c == Float.class) return true;
    if(c == Boolean.class) return true;
    if(c == Character.class) return true;
    if(c == Void.class) return true;
    return false;
  }
}