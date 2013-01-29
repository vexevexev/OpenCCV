


import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import com.sun.opengl.util.GLUT;

public class Console 
{
	public boolean typing, dragging, scrolling;

	public int numLines = 50;
	public String[] entries;
	public String[] lines;
	public String title;
	public String input;
	
	public int scrollHeight;
	public int scrollBarWidth;
	public int scrollBarHeight;
	
	public int width, height;
	public int x, y;
	public int padding;
	
	public int FONT_TYPE;
	public int fontHeight;
	
	public int carrot;
	public int carrotIndex;
	
	public int startIndex;
	public int maxLength;
	
	GLUT glut = new GLUT();
	
	public boolean clickedResizeUpLeft = false;
	public boolean clickedResizeUp = false;
	public boolean clickedResizeUpRight = false;
	public boolean clickedResizeRight = false;
	public boolean clickedResizeDownRight = false;
	public boolean clickedResizeDown = false;
	public boolean clickedResizeDownLeft = false;
	public boolean clickedResizeLeft = false;

	public boolean clickedInput = false;
	public boolean clickedTitle = false;
	public boolean clickedUp = false;
	public boolean clickedDown = false;
	public boolean clickedScrollBar = false;

	public boolean resizing = false;
	
	public int minWidth = 100;
	public int minHeight = 100;
	
	public int[][] projectedResize = new int[4][2];

	private GLU glu = new GLU();
	
	private boolean isVisible = false;
	
	public Console()
	{
		typing = false;

		entries = new String[numLines];
		for(int i = 0; i < entries.length; i++)
			entries[i] = "";
		
		lines = new String[numLines];
		for(int i = 0; i < lines.length; i++)
			lines[i] = "";

		x = 0;
		y = 0;

		width = 100;
		height = 100;
		padding = 7;
		
		fontHeight = 12;
		FONT_TYPE = GLUT.BITMAP_HELVETICA_12;

		title = "Console";
		input = "";
		
		carrot = 0;
		carrotIndex = 0;
		scrollHeight = 0;
		scrollBarWidth = padding*2;

		maxLength = width-padding*2;
		startIndex = 0;
	}

	public void insert(char newChar)
	{
		carrot += glut.glutBitmapWidth(FONT_TYPE, newChar);
		input = input.substring(0, carrotIndex) + newChar + input.substring(carrotIndex, input.length());
		++carrotIndex;
		
		if(carrotIndex < startIndex)
			startIndex++;
		
		while(carrot > maxLength)
		{
			carrot -= glut.glutBitmapWidth(FONT_TYPE, input.charAt(startIndex));
			startIndex++;
		}
	}
	
	
	
	public void append(String newLine)
	{
		for(int i = entries.length-1; i > 0; i--)
			entries[i] = entries[i-1];
		entries[0] = newLine;
		
		appendRecursive(newLine, 0);
	}
	
	private void appendRecursive(String newLine, int stack)
	{
		if(newLine == null || stack > 500) 
			return;
		
		int subLength = 0;
		int index = 0;
		int lastSpace = 0;
		
		
		String temp = "";
		for(; index < newLine.length(); index++)
		{
			subLength += glut.glutBitmapWidth(FONT_TYPE, newLine.charAt(index));
			if(subLength > width-padding*2-scrollBarWidth)
				break;
			temp += newLine.charAt(index);
			if(newLine.charAt(index) == ' ')
				lastSpace = index;
		}
		
		for(int i = lines.length-1; i > 0; i--)
			lines[i] = lines[i-1];
		if(lastSpace!=0 && temp.length() != newLine.length())
			lines[0] = temp.substring(0, lastSpace);
		else
			lines[0] = temp.substring(0, index);
		
		if(temp.length() != newLine.length())
		{
			if(lastSpace != 0)
				appendRecursive(newLine.substring(lastSpace+1), stack+1);
			else
				appendRecursive(newLine.substring(temp.length()), stack+1);
		}
		
		scrollHeight = 0;
	}
	
	public void setVisible(boolean visible)
	{
		System.out.println("asdf");
		if(visible)
		{
			isVisible = true;
			projectedResize[0][0] = x;
			projectedResize[0][1] = y;

			projectedResize[1][0] = x;
			projectedResize[1][1] = y + height;

			projectedResize[2][0] = x + width;
			projectedResize[2][1] = y + height;

			projectedResize[3][0] = x + width;
			projectedResize[3][1] = y;
			
			resize();
		}
		else if(!visible && isVisible)
		{
			isVisible = false;
		}
	}
	
	public void append()
	{
		append(input);
		
		input = "";
		carrot = 0;
		carrotIndex = 0;
		
		scrollHeight = 0;
		startIndex = 0;
	}
	
	public void resize()
	{
		scrollHeight = 0;
		carrot = 0;
		carrotIndex = 0;

		x = projectedResize[0][0];
		y = projectedResize[0][1];

		width = projectedResize[3][0] - projectedResize[0][0];
		height = projectedResize[1][1] - projectedResize[0][1];
		
		scrollBarHeight = (y+height-fontHeight*2-padding) -(y+padding+fontHeight*2);
		
		lines = new String[numLines];
		for(int i = 0; i < lines.length; i++)
			lines[i] = "";
		
		for(int i = entries.length-1; i >= 0; i--)
			appendRecursive(entries[i], 0);
	}
	
	public boolean clickedInput(int xClicked, int yClicked)
	{
		return xClicked >= x && xClicked <= x + width &&
			   yClicked >= y && yClicked <= y+padding+fontHeight;
	}
	public boolean clickedTitle(int xClicked, int yClicked)
	{
		return xClicked >= x && xClicked <= x + width &&
			   yClicked >= y+height-fontHeight-padding && yClicked <= y+height;
	}

	public boolean clickedUp(int xClicked, int yClicked)
	{
		return xClicked >= x+width-scrollBarWidth && xClicked <= x + width &&
			   yClicked >= y+height-fontHeight*2-padding && yClicked <= y+height-fontHeight-padding;
	}


	public boolean clickedDown(int xClicked, int yClicked)
	{
		return xClicked >= x+width-scrollBarWidth && xClicked <= x + width &&
			   yClicked >= y+padding+fontHeight && yClicked <= y+padding+fontHeight*2;
	}
	

	public boolean clickedScrollBar(int xClicked, int yClicked)
	{
		return xClicked >= x+width-scrollBarWidth && xClicked <= x + width &&
			   yClicked >= y+padding+fontHeight*2 && yClicked <= y+height-fontHeight*2-padding;
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public boolean clickedResizeUpLeft(int xClicked, int yClicked)
	{
		//if(xClicked >= x-3 && xClicked <= x+3 && yClicked >= y+height-3 &&  yClicked <= y+height+3) System.out.println("Up-Left");
		return xClicked >= x-3 && xClicked <= x+3 && yClicked >= y+height-3 &&  yClicked <= y+height+3;
	}

	public boolean clickedResizeUp(int xClicked, int yClicked)
	{
		//if(xClicked > x+3 &&  xClicked < x+width-3 && yClicked >= y+height-3 && yClicked <= y+height+3) System.out.println("Up");
		return xClicked > x+3 &&  xClicked < x+width-3 && yClicked >= y+height-3 && yClicked <= y+height+3;
	}

	public boolean clickedResizeUpRight(int xClicked, int yClicked)
	{
		//if(xClicked >= x+width-3 && xClicked <= x+width+3 && yClicked >= y+height-3 && yClicked <= y+height+3) System.out.println("Up-Right");
		return xClicked >= x+width-3 && xClicked <= x+width+3 && yClicked >= y+height-3 && yClicked <= y+height+3;
	}

	public boolean clickedResizeRight(int xClicked, int yClicked)
	{
		//if(xClicked >= x+width-3 && xClicked <= x+width+3 && yClicked < y+height-3 && yClicked > y+3) System.out.println("Right");
		return xClicked >= x+width-3 && xClicked <= x+width+3 && yClicked < y+height-3 && yClicked > y+3;
	}


	public boolean clickedResizeDownRight(int xClicked, int yClicked)
	{
		//if(xClicked >= x+width-3 && xClicked <= x+width+3 && yClicked >= y-3 && yClicked <= y+3) System.out.println("Down-Right");
		return xClicked >= x+width-3 && xClicked <= x+width+3 && yClicked >= y-3 && yClicked <= y+3;
	}

	public boolean clickedResizeDown(int xClicked, int yClicked)
	{
		//if(xClicked < x+width-3 && xClicked > x+3 && yClicked <= y+3 && yClicked >= y-3) System.out.println("Down");
		return xClicked < x+width-3 && xClicked > x+3 && yClicked <= y+3 && yClicked >= y-3;
	}

	public boolean clickedResizeDownLeft(int xClicked, int yClicked)
	{
		//if(xClicked >= x-3 && xClicked <= x+3 && yClicked >= y-3 && yClicked <= y+3) System.out.println("Down-Left");
		return xClicked >= x-3 && xClicked <= x+3 && yClicked >= y-3 && yClicked <= y+3;
	}

	public boolean clickedResizeLeft(int xClicked, int yClicked)
	{
		//if(xClicked >= x-3 && xClicked <= x+3 && yClicked > y+3 && yClicked < y+height-3) System.out.println("Left");
		return xClicked >= x-3 && xClicked <= x+3 && yClicked > y+3 && yClicked < y+height-3;
	}

	public void drawConsole(GL gl) 
	{
		if(!isVisible)
			return;
		
		String tempString;

		gl.glMatrixMode (GL.GL_PROJECTION);
		gl.glLoadIdentity ();
		glu .gluOrtho2D (0, GraphicalMain.RENDER_WIDTH, 0, GraphicalMain.RENDER_HEIGHT);
		gl.glMatrixMode (GL.GL_MODELVIEW);
		gl.glLoadIdentity ();
		gl.glTranslatef (0.375f, 0.375f, 0);
		
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glColor4f(0,0,0,0.5f);
		gl.glBegin(GL.GL_QUADS);
		gl.glVertex2i(x, y);
		gl.glVertex2i(x+width, y);
		gl.glVertex2i(x+width, y+height);
		gl.glVertex2i(x, y+height);
		gl.glEnd();
		gl.glDisable(GL.GL_BLEND);
		
		gl.glColor3f(0,0,0);
		gl.glBegin(GL.GL_QUADS);
		gl.glVertex2i(x, y);
		gl.glVertex2i(x+width, y);
		gl.glVertex2i(x+width, y+padding+fontHeight);
		gl.glVertex2i(x, y+padding+fontHeight);
		gl.glEnd();
		
		gl.glColor3f(1,1,1);

		gl.glRasterPos2i(x+padding, y+padding);
		
		int inputWidth = 0;
		tempString = input;
		for(int i = startIndex; i < tempString.length(); i++)
		{
			inputWidth += glut.glutBitmapWidth(FONT_TYPE, tempString.charAt(i));
			if(inputWidth > maxLength)
				break;
			glut.glutBitmapCharacter(FONT_TYPE, tempString.charAt(i));
		}
		
		int lineNumber = (int)(((double)scrollHeight/(double)scrollBarHeight)*(double)numLines);
		for(int accumulatedHeight = y+padding*2+fontHeight; 
				accumulatedHeight < y+height-padding-fontHeight &&
				lineNumber < numLines; 
				accumulatedHeight += fontHeight+padding)
		{
			gl.glRasterPos2i(x+padding, accumulatedHeight);
			tempString = lines[lineNumber];
			
			for(int i = 0; i < tempString.length(); i++)
			{
				glut.glutBitmapCharacter(FONT_TYPE, tempString.charAt(i));
				//width += gl.glut.gl.glutBitmapWidth(GLUT_BITMAP_TIMES_ROMAN_fontHeight, *c);
			}
			lineNumber++;
		}

		//Draw Carrot
		if(typing)
		{
			gl.glColor3f(1, 1, 1);
			gl.glBegin(GL.GL_LINES);
			gl.glVertex2i(x+padding+carrot, y+padding/2+fontHeight);
			gl.glVertex2i(x+padding+carrot, y+padding/2);
			gl.glEnd();
		}
		
		//Draw Opaque Backdrops
		gl.glColor3f(0,0,0);
		gl.glBegin(GL.GL_QUADS);
		gl.glVertex2i(x, y+height);
		gl.glVertex2i(x+width, y+height);
		gl.glVertex2i(x+width, y+height-fontHeight-padding);
		gl.glVertex2i(x, y+height-fontHeight-padding);
		gl.glEnd();
		
		//DrawScrollBar backdrop
		/*
		gl.glBegin(GL.GL_QUADS);
		gl.glVertex2i(x+width-scrollBarWidth, y+padding+fontHeight);
		gl.glVertex2i(x+width-scrollBarWidth, y+height-fontHeight-padding);
		gl.glVertex2i(x+width, y+height-fontHeight-padding);
		gl.glVertex2i(x+width, y+padding+fontHeight);
		gl.glEnd();
		*/
		
		gl.glColor3f(1,1,1);
		gl.glRasterPos2i(x+padding, y+height-fontHeight);
		tempString = title;
		for(int i = 0; i < tempString.length(); i++)
		{
			glut.glutBitmapCharacter(FONT_TYPE, tempString.charAt(i));
			//width += gl.glut.gl.glutBitmapWidth(GLUT_BITMAP_TIMES_ROMAN_fontHeight, *c);
		}

		gl.glColor3f(1,1,0);
		gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex2i(x, y);
		gl.glVertex2i(x+width, y);
		gl.glVertex2i(x+width, y+height);
		gl.glVertex2i(x, y+height);
		gl.glEnd();
		gl.glBegin(GL.GL_LINES);
		gl.glVertex2i(x, y+height-fontHeight-padding);
		gl.glVertex2i(x+width, y+height-fontHeight-padding);
		gl.glVertex2i(x, y+padding+fontHeight);
		gl.glVertex2i(x+width, y+padding+fontHeight);
		gl.glEnd();
		
		//DrawScrollBar
		gl.glBegin(GL.GL_LINES);
		
		//Vertical Bar
		gl.glVertex2i(x+width-scrollBarWidth, y+padding+fontHeight);
		gl.glVertex2i(x+width-scrollBarWidth, y+height-fontHeight-padding);
		
		//Upper horizontal Bar
		gl.glVertex2i(x+width-scrollBarWidth, y+height-fontHeight*2-padding);
		gl.glVertex2i(x+width, y+height-fontHeight*2-padding);

		//Lower horizontal Bar
		gl.glVertex2i(x+width-scrollBarWidth, y+padding+fontHeight*2);
		gl.glVertex2i(x+width, y+padding+fontHeight*2);

		//Navigating Bar
		gl.glVertex2i(x+width-scrollBarWidth, y+padding+fontHeight*2+scrollHeight);
		gl.glVertex2i(x+width, y+padding+fontHeight*2+scrollHeight);
		
		gl.glEnd();
		
		//DrawScrollBar Up Arrow
		gl.glBegin(GL.GL_TRIANGLES);

		gl.glVertex2i(x+width-scrollBarWidth, y+height-fontHeight*2-padding+3);
		gl.glVertex2i((x+width-scrollBarWidth+x+width)/2, 
					   y+height-fontHeight-padding-3);
		gl.glVertex2i(x+width, y+height-fontHeight*2-padding+3);
		
		//DrawScrollBar Down Arrow
		gl.glVertex2i(x+width-scrollBarWidth, y+padding+fontHeight*2-3);
		gl.glVertex2i(x+width-scrollBarWidth/2, y+padding+fontHeight+3);
		gl.glVertex2i(x+width, y+padding+fontHeight*2-3);
		
		gl.glEnd();
		
		//gl.glColor3f(1,1,1); drawObjectStats();
		
		//Draw Resizing Projection
		if(resizing)
		{
			gl.glColor3f(1,1,1);
			gl.glBegin(GL.GL_LINE_LOOP);
			gl.glVertex2i(projectedResize[0][0], projectedResize[0][1]);
			gl.glVertex2i(projectedResize[1][0], projectedResize[1][1]);
			gl.glVertex2i(projectedResize[2][0], projectedResize[2][1]);
			gl.glVertex2i(projectedResize[3][0], projectedResize[3][1]);
			gl.glEnd();
		}
		
		if(clickedDown)
			scrollHeight -= 1;
		else if(clickedUp)
			scrollHeight += 1;
		if(scrollHeight < 0)
			scrollHeight = 0;
		if(scrollHeight > height-fontHeight*4-2*padding)
			scrollHeight = height-fontHeight*4-2*padding;
	}

	public void handleLeftClick(int clickX, int clickY) 
	{
		clickedInput = clickedInput(clickX, clickY);
		clickedTitle = clickedTitle(clickX, clickY);
		clickedUp = clickedUp(clickX, clickY);
		clickedDown = clickedDown(clickX, clickY);
		clickedScrollBar = clickedScrollBar(clickX, clickY);
		
		clickedResizeUpLeft = clickedResizeUpLeft(clickX, clickY);
		clickedResizeUp = clickedResizeUp(clickX, clickY);
		clickedResizeUpRight = clickedResizeUpRight(clickX, clickY);
		clickedResizeRight = clickedResizeRight(clickX, clickY);
		clickedResizeDownRight = clickedResizeDownRight(clickX, clickY);
		clickedResizeDown = clickedResizeDown(clickX, clickY);
		clickedResizeDownLeft = clickedResizeDownLeft(clickX, clickY);
		clickedResizeLeft = clickedResizeLeft(clickX, clickY);
		

		if(!resizing && (clickedResizeUpLeft || clickedResizeUp || clickedResizeUpRight || clickedResizeRight ||
		clickedResizeDownRight || clickedResizeDown || clickedResizeDownLeft || clickedResizeLeft))
		{
			resizing = true;
			
			projectedResize[0][0] = x;
			projectedResize[0][1] = y;

			projectedResize[1][0] = x;
			projectedResize[1][1] = y + height;

			projectedResize[2][0] = x + width;
			projectedResize[2][1] = y + height;

			projectedResize[3][0] = x + width;
			projectedResize[3][1] = y;
		}

		if(!resizing && clickedInput)
			typing = true;
		else
			typing = false;

		if(!resizing && clickedTitle)
			dragging = true;

		if(!resizing && clickedUp)
		{
			scrolling = true;
			
			scrollHeight += 1;
			if(scrollHeight > height-fontHeight*4-2*padding)
				scrollHeight = height-fontHeight*4-2*padding;
		}

		if(!resizing && clickedDown)
		{
			scrolling = true;
			
			scrollHeight -= 1;
			if(scrollHeight < 0)
				scrollHeight = 0;
		}
		
		if(!resizing && clickedScrollBar)
		{
			scrolling = true;
		}
	}

	public void handleRightClick(int clickX, int clickY) 
	{
		clickedInput = clickedInput(clickX, clickY);
		clickedTitle = clickedTitle(clickX, clickY);
		if(!clickedTitle && !clickedInput)
			typing = false;	
	}
	
	public void handleDrag(int dragX, int dragY)
	{
		if(!resizing && scrolling && clickedScrollBar)
			scrollHeight = (GraphicalMain.RENDER_HEIGHT-User.lastMouseY)-y-padding-fontHeight*2;

		if(dragging)
		{
			x += (int)(dragX - User.lastMouseX);
			y -= (int)(dragY - User.lastMouseY);

			if(x < 0)
				x = 0;
			else if(x+width > GraphicalMain.RENDER_WIDTH-2)
				x = (int) (GraphicalMain.RENDER_WIDTH-width-2);

				
			if(y < 0)
				y = 0;
			else if(y+height > GraphicalMain.RENDER_HEIGHT-2)
				y = (int) (GraphicalMain.RENDER_HEIGHT-height-2);
		}
		else if(scrolling)
		{
			scrollHeight += (int)(dragY - User.lastMouseY);

			if(scrollHeight < 0)
				scrollHeight = 0;

			if(scrollHeight > height-fontHeight*4-2*padding)
				scrollHeight = height-fontHeight*4-2*padding;
		}
		else if(resizing)
		{
			if(clickedResizeUpLeft)
			{
				projectedResize[1][0] += dragX - User.lastMouseX;
				projectedResize[1][1] -= dragY - User.lastMouseY;
				projectedResize[0][0] += dragX - User.lastMouseX;
				projectedResize[2][1] -= dragY - User.lastMouseY;
				
				if(projectedResize[2][0]-projectedResize[1][0] < minWidth)
				{
					projectedResize[1][0] -= dragX - User.lastMouseX;
					projectedResize[0][0] -= dragX - User.lastMouseX;
				}
				if(projectedResize[1][1]-projectedResize[0][1] < minHeight)
				{
					projectedResize[1][1] += dragY - User.lastMouseY;
					projectedResize[2][1] += dragY - User.lastMouseY;
				}
			}
			else if(clickedResizeUp)
			{
				projectedResize[1][1] -= dragY - User.lastMouseY;
				projectedResize[2][1] -= dragY - User.lastMouseY;

				if(projectedResize[1][1]-projectedResize[0][1] < minHeight)
				{
					projectedResize[1][1] += dragY - User.lastMouseY;
					projectedResize[2][1] += dragY - User.lastMouseY;
				}
			}
			else if(clickedResizeUpRight)
			{
				projectedResize[2][0] += dragX - User.lastMouseX;
				projectedResize[2][1] -= dragY - User.lastMouseY;
				projectedResize[1][1] -= dragY - User.lastMouseY;
				projectedResize[3][0] += dragX - User.lastMouseX;
				
				if(projectedResize[2][0]-projectedResize[1][0] < minWidth)
				{
					projectedResize[2][0] -= dragX - User.lastMouseX;
					projectedResize[3][0] -= dragX - User.lastMouseX;
				}
				if(projectedResize[1][1]-projectedResize[0][1] < minHeight)
				{
					projectedResize[1][1] += dragY - User.lastMouseY;
					projectedResize[2][1] += dragY - User.lastMouseY;
				}
			}
			else if(clickedResizeRight)
			{
				projectedResize[2][0] += dragX - User.lastMouseX;
				projectedResize[3][0] += dragX - User.lastMouseX;
				
				if(projectedResize[2][0]-projectedResize[1][0] < minWidth)
				{
					projectedResize[2][0] -= dragX - User.lastMouseX;
					projectedResize[3][0] -= dragX - User.lastMouseX;
				}
			}
			else if(clickedResizeDownRight)
			{
				projectedResize[3][0] += dragX - User.lastMouseX;
				projectedResize[3][1] -= dragY - User.lastMouseY;
				projectedResize[2][0] += dragX - User.lastMouseX;
				projectedResize[0][1] -= dragY - User.lastMouseY;
				
				if(projectedResize[2][0]-projectedResize[1][0] < minWidth)
				{
					projectedResize[2][0] -= dragX - User.lastMouseX;
					projectedResize[3][0] -= dragX - User.lastMouseX;
				}
				if(projectedResize[1][1]-projectedResize[0][1] < minHeight)
				{
					projectedResize[3][1] += dragY - User.lastMouseY;
					projectedResize[0][1] += dragY - User.lastMouseY;
				}
			}
			else if(clickedResizeDown)
			{
				projectedResize[3][1] -= dragY - User.lastMouseY;
				projectedResize[0][1] -= dragY - User.lastMouseY;

				if(projectedResize[2][1]-projectedResize[3][1] < minHeight)
				{
					projectedResize[3][1] += dragY - User.lastMouseY;
					projectedResize[0][1] += dragY - User.lastMouseY;
				}
			}
			else if(clickedResizeDownLeft)
			{
				projectedResize[0][0] += dragX - User.lastMouseX;
				projectedResize[0][1] -= dragY - User.lastMouseY;
				projectedResize[1][0] += dragX - User.lastMouseX;
				projectedResize[3][1] -= dragY - User.lastMouseY;
				
				if(projectedResize[3][0]-projectedResize[0][0] < minWidth)
				{
					projectedResize[0][0] -= dragX - User.lastMouseX;
					projectedResize[1][0] -= dragX - User.lastMouseX;
				}
				if(projectedResize[2][1]-projectedResize[0][1] < minHeight)
				{
					projectedResize[0][1] += dragY - User.lastMouseY;
					projectedResize[3][1] += dragY - User.lastMouseY;
				}
			}
			else if(clickedResizeLeft)
			{
				projectedResize[0][0] += dragX - User.lastMouseX;
				projectedResize[1][0] += dragX - User.lastMouseX;

				if(projectedResize[2][0]-projectedResize[1][0] < minHeight)
				{
					projectedResize[0][0] -= dragX - User.lastMouseX;
					projectedResize[1][0] -= dragX - User.lastMouseX;
				}
			}
			
			if(projectedResize[0][0] < 1)
			{
				projectedResize[0][0] = 1;
				projectedResize[1][0] = 1;
			}
			if(projectedResize[1][1] >= GraphicalMain.RENDER_HEIGHT-2)
			{
				projectedResize[1][1] = (int)GraphicalMain.RENDER_HEIGHT-2;
				projectedResize[2][1] = (int)GraphicalMain.RENDER_HEIGHT-2;					
			}
			if(projectedResize[2][0] >= GraphicalMain.RENDER_WIDTH-2)
			{
				projectedResize[2][0] = (int)GraphicalMain.RENDER_WIDTH-2;
				projectedResize[3][0] = (int)GraphicalMain.RENDER_WIDTH-2;
			}
			if(projectedResize[3][1] < 1)
			{
				projectedResize[3][1] = 1;
				projectedResize[0][1] = 1;
			}
		}
	}
	boolean isWritable(char c)
	{
		switch(c)
		{
			case ' ': return true;
			case 'a': return true;
			case 'b': return true;
			case 'c': return true;
			case 'd': return true;
			case 'e': return true;
			case 'f': return true;
			case 'g': return true;
			case 'h': return true;
			case 'i': return true;
			case 'j': return true;
			case 'k': return true;
			case 'l': return true;
			case 'm': return true;
			case 'n': return true;
			case 'o': return true;
			case 'p': return true;
			case 'q': return true;
			case 'r': return true;
			case 's': return true;
			case 't': return true;
			case 'u': return true;
			case 'v': return true;
			case 'w': return true;
			case 'x': return true;
			case 'y': return true;
			case 'z': return true;
			case 'A': return true;
			case 'B': return true;
			case 'C': return true;
			case 'D': return true;
			case 'E': return true;
			case 'F': return true;
			case 'G': return true;
			case 'H': return true;
			case 'I': return true;
			case 'J': return true;
			case 'K': return true;
			case 'L': return true;
			case 'M': return true;
			case 'N': return true;
			case 'O': return true;
			case 'P': return true;
			case 'Q': return true;
			case 'R': return true;
			case 'S': return true;
			case 'T': return true;
			case 'U': return true;
			case 'V': return true;
			case 'W': return true;
			case 'X': return true;
			case 'Y': return true;
			case 'Z': return true;
	
			case '0': return true;
			case '1': return true;
			case '2': return true;
			case '3': return true;
			case '4': return true;
			case '5': return true;
			case '6': return true;
			case '7': return true;
			case '8': return true;
			case '9': return true;
	
			case '!': return true;
			case '@': return true;
			case '#': return true;
			case '$': return true;
			case '%': return true;
			case '^': return true;
			case '&': return true;
			case '*': return true;
			case '(': return true;
			case ')': return true;
	
			case '`': return true;
			case '~': return true;
			case '-': return true;
			case '_': return true;
			case '=': return true;
			case '+': return true;
			case '[': return true;
			case '{': return true;
			case ']': return true;
			case '}': return true;
	
			case '\\': return true;
			case '|': return true;
			case '\'': return true;
			case '"': return true;
			case ';': return true;
			case ':': return true;
			case '/': return true;
			case '?': return true;
			case '.': return true;
			case '>': return true;
			
			case ',': return true;
			case '<': return true;
			
			default: return false;
		}
	}

	public boolean isVisible() 
	{
		return isVisible;
	}
}
