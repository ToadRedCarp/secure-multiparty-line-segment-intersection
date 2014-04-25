import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.layout.FillLayout;
import swing2swt.layout.FlowLayout;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;


public class Window
{

	protected Shell shell;
	private Text ipAddressText;
	private Text portText;
	private Text statusText;
	
	private Point mouseMove = new Point(0, 0);
	private Point mouseDown = new Point(0, 0);
	private Point mouseUp   = new Point(0, 0);
  private boolean lineDrawnFlag = false;
  
  private String ipAddress = "";
  private Integer port;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			Window window = new Window();
			window.open();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open()
	{
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
			{
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents()
	{
		shell = new Shell();
		shell.setSize(487, 406);
		shell.setText("SWT Application");
		shell.setLayout(new GridLayout(3, false));
		
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
		composite.setLayout(new GridLayout(2, false));
		
		Label lblSecondParty = new Label(composite, SWT.NONE);
		lblSecondParty.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblSecondParty.setText("Second Party - IP Address");
		
		ipAddressText = new Text(composite, SWT.BORDER);
		
		ipAddressText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				ipAddress = ipAddressText.getText();
			}
		});
		
		ipAddressText.setText("localhost");
		ipAddressText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblSecondParty_1 = new Label(composite, SWT.NONE);
		lblSecondParty_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblSecondParty_1.setText("Second Party - Port");
		
		portText = new Text(composite, SWT.BORDER);
		
		portText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				port = Integer.parseInt(portText.getText());
			}
		});
		portText.setText("9090");
		portText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		statusText = new Text(shell, SWT.BORDER | SWT.MULTI);
		GridData gd_text_2 = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 3);
		gd_text_2.widthHint = 204;
		statusText.setLayoutData(gd_text_2);
		
		final Canvas canvas = new Canvas(shell, SWT.BORDER);
		
		canvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent arg0) {
				mouseDown.x = mouseMove.x;
				mouseDown.y = mouseMove.y;
				lineDrawnFlag = false;
			}
			@Override
			public void mouseUp(MouseEvent arg0) {
				mouseUp.x = mouseMove.x;
				mouseUp.y = mouseMove.y;
				lineDrawnFlag = true;
				canvas.redraw();
			}
		});
		
		canvas.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent arg0) {
				mouseMove.x = arg0.x;
				mouseMove.y = arg0.y;
			}
		});
		
		GridData gd_canvas = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2);
		gd_canvas.heightHint = 250;
		gd_canvas.widthHint = 250;
		canvas.setLayoutData(gd_canvas);
		canvas.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BORDER));
		
		    canvas.addPaintListener(new PaintListener() { 

						public void paintControl(PaintEvent e) { 
		            Point size = canvas.getSize();
		            e.gc.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_GRAY));  
		            
		            // Draw Coordinate Plane
		            e.gc.setLineWidth(3);
		            Integer pad = 20;
		            Integer arrowLength = 7;
		            e.gc.drawLine(pad, size.y-pad, size.x-pad, size.y-pad);
		            e.gc.drawLine(pad, size.y-pad, pad, pad);
		            
		            // Draw Arrowheads
		            e.gc.setLineWidth(2);
		            
		            e.gc.drawLine(pad, pad, pad-arrowLength, pad+arrowLength);
		            e.gc.drawLine(pad, pad, pad+arrowLength, pad+arrowLength);
		           
		            e.gc.drawLine(
		            		size.x-pad, 
		            		size.y-pad, 
		            		size.x-pad-arrowLength, 
		            		size.y-pad+arrowLength); 
		            e.gc.drawLine(
		            		size.x-pad, 
		            		size.y-pad, 
		            		size.x-pad-arrowLength, 
		            		size.y-pad-arrowLength); 
		           
		            // Draw gridlines
		            e.gc.setLineWidth(1);
		            Point numberOfGridLines = new Point(10,10);
		            Point gridSize = new Point((size.x-2*pad)/numberOfGridLines.x, (size.y-2*pad)/numberOfGridLines.y);
		            for(int i = 0; i <= numberOfGridLines.x; i++) {
		            	e.gc.drawLine(
		            		pad + i*gridSize.x, 
		            		size.y-pad, 
		            		pad + i*gridSize.x, 
		            		pad); 
		            }
		            
		            for(int i = 0; i <= numberOfGridLines.y; i++)  {
		            	e.gc.drawLine(
		            		pad,
		            		pad + i*gridSize.y, 
		            		size.x-pad, 
		            		pad + i*gridSize.y); 
		            }

		            if (lineDrawnFlag) {
			            e.gc.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_BLUE)); 
			            e.gc.setLineWidth(3);
			            e.gc.drawLine(
			            		mouseDown.x, 
			            		mouseDown.y, 
			            		mouseUp.x, 
			            		mouseUp.y);
		            }
		            
		        } 
		    });
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		
		Composite composite_1 = new Composite(shell, SWT.NONE);
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1));
		composite_1.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		Composite composite_3 = new Composite(composite_1, SWT.NONE);
		composite_3.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		
		Composite composite_2 = new Composite(composite_1, SWT.NONE);
		composite_2.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
		
		Button btnNewButton = new Button(composite_2, SWT.NONE);
		
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				new App(port, ipAddress, mouseDown, mouseUp, statusText);
			}
		});
		
		btnNewButton.setText("Submit");
		
		Composite composite_5 = new Composite(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
	}
}
