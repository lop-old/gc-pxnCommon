package com.poixson.utils.byref;


public interface StringRefInterface {



	public void value(final String val);
	public String value();

	public boolean isEmpty();
	public boolean notEmpty();

	public int length();



	// ------------------------------------------------------------------------------- //
	// index of



	// index of (single delim)
	public int indexOf(final char delim);
	public int indexOf(final String delim);

	// index of (many delims)
	public int indexOf(final char...delims);
	public int indexOf(final String...delims);

	// last index of (single delim)
	public int indexOfLast(final char delim);
	public int indexOfLast(final String delim);

	// last index of (many delims)
	public int indexOfLast(final char...delims);
	public int indexOfLast(final String...delims);



	// ------------------------------------------------------------------------------- //
	// get/cut first part



	// get first part (single delim)
	public String PeekFirstPart(final char   delim);
	public String PeekFirstPart(final String delim);
	// cut first part (single delim)
	public String CutFirstPart(final char    delim);
	public String CutFirstPart(final String  delim);

	// get first part (many delims)
	public String PeekFirstPart(final char  ...delims);
	public String PeekFirstPart(final String...delims);
	// cut first part (many delims)
	public String CutFirstPart(final char   ...delims);
	public String CutFirstPart(final String ...delims);



	// ------------------------------------------------------------------------------- //
	// get/cut last part



	// get last part (single delim)
	public String PeekLastPart(final char   delim);
	public String PeekLastPart(final String delim);
	// cut last part (single delim)
	public String CutLastPart(final char    delim);
	public String CutLastPart(final String  delim);

	// get last part (many delims)
	public String PeekLastPart(final char  ...delims);
	public String PeekLastPart(final String...delims);
	// cut last part (many delims)
	public String CutLastPart(final char   ...delims);
	public String CutLastPart(final String ...delims);



}
