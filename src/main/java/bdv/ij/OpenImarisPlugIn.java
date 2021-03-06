package bdv.ij;

import ij.ImageJ;
import ij.Prefs;
import ij.plugin.PlugIn;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import bdv.BigDataViewer;
import bdv.ij.util.ProgressWriterIJ;
import bdv.img.imaris.Imaris;
import bdv.spimdata.SpimDataMinimal;

public class OpenImarisPlugIn implements PlugIn
{
	static String lastDatasetPath = "";

	public static void main( final String[] args )
	{
		ImageJ.main( args );
		new OpenImarisPlugIn().run( null );
	}
	@Override
	public void run( final String arg )
	{
		File file = null;

		if ( Prefs.useJFileChooser )
		{
			final JFileChooser fileChooser = new JFileChooser();
			fileChooser.setSelectedFile( new File( lastDatasetPath ) );
			fileChooser.setFileFilter( new FileFilter()
			{
				@Override
				public String getDescription()
				{
					return "ims files";
				}

				@Override
				public boolean accept( final File f )
				{
					if ( f.isDirectory() )
						return true;
					if ( f.isFile() )
					{
				        final String s = f.getName();
				        final int i = s.lastIndexOf('.');
				        if (i > 0 &&  i < s.length() - 1) {
				            final String ext = s.substring(i+1).toLowerCase();
				            return ext.equals( "ims" );
				        }
					}
					return false;
				}
			} );

			final int returnVal = fileChooser.showOpenDialog( null );
			if ( returnVal == JFileChooser.APPROVE_OPTION )
				file = fileChooser.getSelectedFile();
		}
		else // use FileDialog
		{
			final FileDialog fd = new FileDialog( ( Frame ) null, "Open", FileDialog.LOAD );
			fd.setDirectory( new File( lastDatasetPath ).getParent() );
			fd.setFile( new File( lastDatasetPath ).getName() );
			final AtomicBoolean workedWithFilenameFilter = new AtomicBoolean( false );
			fd.setFilenameFilter( new FilenameFilter()
			{
				private boolean firstTime = true;

				@Override
				public boolean accept( final File dir, final String name )
				{
					if ( firstTime )
					{
						workedWithFilenameFilter.set( true );
						firstTime = false;
					}

					final int i = name.lastIndexOf( '.' );
					if ( i > 0 && i < name.length() - 1 )
					{
						final String ext = name.substring( i + 1 ).toLowerCase();
						return ext.equals( "ims" );
					}
					return false;
				}
			} );
			fd.setVisible( true );
			if ( !workedWithFilenameFilter.get() )
			{
				fd.setFilenameFilter( null );
				fd.setVisible( true );
			}
			final String filename = fd.getFile();
			if ( filename != null )
			{
				file = new File( fd.getDirectory() + filename );
			}
		}

		if ( file != null )
		{
			try
			{
				lastDatasetPath = file.getAbsolutePath();
				final SpimDataMinimal spimData = Imaris.openIms( file.getAbsolutePath() );
				new BigDataViewer( spimData, file.getName(), new ProgressWriterIJ() );
			}
			catch ( final IOException e )
			{
				throw new RuntimeException( e );
			}
		}
	}
}
