/*
 * A replacement for java.util.StringTokenizer
 * Copyright (C) 2001 Stephen Ostermiller
 * http://ostermiller.org/contact.pl?regarding=Java+Utilities
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * See COPYING.TXT for details.
 */

package util;
/**
 * The string tokenizer class allows an application to break a string into
 * tokens.
 * More information about this class is available from <a target="_top" href=
 * "http://ostermiller.org/utils/StringTokenizer.html">ostermiller.org</a>.
 * <p>
 * The tokenization method is much simpler than the one used by the
 * <code>StreamTokenizer</code> class. The <code>StringTokenizer</code> methods
 * do not distinguish among identifiers, numbers, and quoted strings, nor do
 * they recognize and skip comments.
 * <p>
 * The set of delimiters (the characters that separate tokens) may be specified
 * either at creation time or on a per-token basis.
 * <p>
 * There are two kinds of delimiters: token delimiters and nontoken delimiters.
 * A token is either one token delimiter character, or a maximal sequence of
 * consecutive characters that are not delimiters.
 * <p>
 * A <code>StringTokenizer</code> object internally maintains a current
 * position within the string to be tokenized. Some operations advance this
 * current position past the characters processed.
 * <p>
 * The implementation is not thread safe; if a <code>StringTokenizer</code>
 * object is intended to be used in multiple threads, an appropriate wrapper
 * must be provided.
 * <p>
 * The following is one example of the use of the tokenizer. It also
 * demonstrates the usefulness of having both token and nontoken delimiters in
 * one <code>StringTokenizer</code>.
 * <p>
 * The code:
 * <blockquote><code>
 * String s = " &nbsp;( &nbsp; aaa  \t &nbsp;* (b+c1 ))";<br>
 * StringTokenizer st = new StringTokenizer(s, " \t\n\r\f", "()+*");<br>
 * while (st.hasMoreTokens()) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;System.out.println(st.nextToken());<br>
 * };
 * </code></blockquote>
 * <p>
 * prints the following output:
 * <blockquote>
 * (<br>
 * aaa<br>
 * *<br>
 * (<br>
 * b<br>
 * +<br>
 * c1<br>
 * )<br>
 * )
 * </blockquote>
 * <p>
 * </b>Compatibility with <code>java.util.StringTokenizer</code></b>
 * <p>
 * In the original version of <code>java.util.StringTokenizer</code>, the method
 * <code>nextToken()</code> left the current position after the returned token,
 * and the method <code>hasMoreTokens()</code> moved (as a side effect) the
 * current position before the beginning of the next token. Thus, the code:
 * <blockquote><code>
 * String s = "x=a,b,c";<br>
 * java.util.StringTokenizer st = new java.util.StringTokenizer(s,"=");<br>
 * System.out.println(st.nextToken());<br>
 * while (st.hasMoreTokens()) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;System.out.println(st.nextToken(","));<br>
 * };
 * </code></blockquote>
 * <p>
 * prints the following output:
 * <blockquote>
 * x<br>
 * a<br>
 * b<br>
 * c
 * </blockquote>
 * <p>
 * The Java SDK 1.3 implementation removed the undesired side effect of
 * <code>hasMoreTokens</code> method: now, it does not advance current position.
 * However, after these changes the output of the above code was:
 * <blockquote>
 * x<br>
 * =a<br>
 * b<br>
 * c
 * </blockquote>
 * <p>
 * and there was no good way to produce a second token without "=".
 * <p>
 * To solve the problem, this implementation introduces a new method
 * <code>skipDelimiters()</code>. To produce the original output, the above code
 * should be modified as:
 * <blockquote><code>
 * String s = "x=a,b,c";<br>
 * StringTokenizer st = new StringTokenizer(s,"=");<br>
 * System.out.println(st.nextToken());<br>
 * st.skipDelimiters();<br>
 * while (st.hasMoreTokens()) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;System.out.println(st.nextToken(","));<br>
 * };
 * </code></blockquote>
 *
 * @author Stephen Ostermiller http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * @since ostermillerutils 1.00.00
 */
public class StringTokenizer implements java.util.Enumeration{

	/**
	 * The string to be tokenized.
	 * The code relies on this to never be null.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	protected String text;

	/**
	 * The length of the text.
	 * Cached for performance.  This should be set whenever the
	 * string we are working with is changed.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	protected int strLength;

	/**
	 * The set of nontoken delimiters.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	protected String nontokenDelims;

	/**
	 * The set of token delimiters.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	protected String tokenDelims;

	/**
	 * One of two variables used to maintain state through
	 * the tokenizing process.
	 * <P>
	 * Represents the position at which we should start looking for
	 * the next token(the position of the character immediately
	 * following the end of the last token, or 0 to start), or
	 * -1 if the entire string has been examined.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	protected int position;

	/**
	 * One of two variables used to maintain state through
	 * the tokenizing process.
	 * <p>
	 * true if and only if is found that an empty token should
	 * be returned or if empty token was the last thing returned.
	 * <p>
	 * If returnEmptyTokens in false, then this variable will
	 * always be false.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	protected boolean emptyReturned;

	/**
	 * Stores the value of the delimiter character with the
	 * highest value. It is used to optimize the detection of delimiter
	 * characters.  The common case will be that the int values of delimiters
	 * will be less than that of most characters in the string (, or space less
	 * than any letter for example).  Given this, we can check easily check
	 * to see if a character is not a delimiter by comparing it to the max
	 * delimiter.  If it is greater than the max delimiter, then it is no
	 * a delimiter otherwise we have to do some more in depth analysis. (ie
	 * search the delimiter string.)  This will reduce the running time of
	 * the algorithm not to depend on the length of the delimiter string
	 * for the common case.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	protected char maxDelimChar;

	/**
	 * Whether empty tokens should be returned.
	 * ie if "" should be returned when text starts with
	 * a delim, has two delims next to each other, or
	 * ends with a delim.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	protected boolean returnEmptyTokens;

	/**
	 * Indicates at which position the delimiters last changed.  This
	 * will effect how null tokens are returned.  Any
	 * time that delimiters are changed, the string will be treated as if
	 * it is being parsed from position zero, ie, null strings are possible
	 * at the very beginning.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	protected int delimsChangedPosition;

	/**
	 * A cache of the token count.  This variable should be -1 if the token
	 * have not yet been counted. It should be greater than or equal to zero
	 * if the tokens have been counted.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	protected int tokenCount;

	/**
	 * Constructs a string tokenizer for the specified string. Both token and
	 * nontoken delimiters are specified.
	 * <p>
	 * The current position is set at the beginning of the string.
	 *
	 * @param text a string to be parsed.
	 * @param nontokenDelims the nontoken delimiters, i.e. the delimiters that only separate
	 *     tokens and are not returned as separate tokens.
	 * @param tokenDelims the token delimiters, i.e. delimiters that both separate tokens,
	 *     and are themselves returned as tokens.
	 * @throws NullPointerException if text is null.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public StringTokenizer(String text, String nontokenDelims, String tokenDelims){
		this(text, nontokenDelims, tokenDelims, false);
	}

	/**
	 * Constructs a string tokenizer for the specified string. Both token and
	 * nontoken delimiters are specified and whether or not empty tokens are returned
	 * is specified.
	 * <p>
	 * Empty tokens are tokens that are between consecutive delimiters.
	 * <p>
	 * It is a primary constructor (i.e. all other constructors are defined in terms
	 * of it.)
	 * <p>
	 * The current position is set at the beginning of the string.
	 *
	 * @param text a string to be parsed.
	 * @param nontokenDelims the nontoken delimiters, i.e. the delimiters that only separate
	 *     tokens and are not returned as separate tokens.
	 * @param tokenDelims the token delimiters, i.e. delimiters that both separate tokens,
	 *     and are themselves returned as tokens.
	 * @param returnEmptyTokens true if empty tokens may be returned; false otherwise.
	 * @throws NullPointerException if text is null.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public StringTokenizer(String text, String nontokenDelims, String tokenDelims, boolean returnEmptyTokens){
		setDelims(nontokenDelims, tokenDelims);
		setText(text);
		setReturnEmptyTokens(returnEmptyTokens);
	}

	/**
	 * Constructs a string tokenizer for the specified string. Either token or
	 * nontoken delimiters are specified.
	 * <p>
	 * Is equivalent to:
	 * <ul>
	 * <li> If the third parameter is <code>false</code> --
	 *      <code>StringTokenizer(text,delims, null)</code>
	 * <li> If the third parameter is <code>true</code> --
	 *      <code>StringTokenizer(text, null ,delims)</code>
	 * </ul>
	 *
	 * @param text a string to be parsed.
	 * @param delims the delimiters.
	 * @param delimsAreTokens
	 *     flag indicating whether the second parameter specifies token or
	 *     nontoken delimiters: <code>false</code> -- the second parameter
	 *     specifies nontoken delimiters, the set of token delimiters is
	 *     empty; <code>true</code> -- the second parameter specifies token
	 *     delimiters, the set of nontoken delimiters is empty.
	 * @throws NullPointerException if text is null.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public StringTokenizer(String text, String delims, boolean delimsAreTokens){
		this(text, (delimsAreTokens ? null : delims), (delimsAreTokens ? delims : null));
	}

	/**
	 * Constructs a string tokenizer for the specified string. The characters in the
	 * <code>nontokenDelims</code> argument are the delimiters for separating
	 * tokens. Delimiter characters themselves will not be treated as tokens.
	 * <p>
	 * Is equivalent to <code>StringTokenizer(text,nontokenDelims, null)</code>.
	 *
	 * @param text a string to be parsed.
	 * @param nontokenDelims the nontoken delimiters.
	 * @throws NullPointerException if text is null.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public StringTokenizer(String text, String nontokenDelims){
		this(text, nontokenDelims, null);
	}

	/**
	 * Constructs a string tokenizer for the specified string. The tokenizer uses
	 * " \t\n\r\f" as a delimiter set of nontoken delimiters, and an empty token
	 * delimiter set.
	 * <p>
	 * Is equivalent to <code>StringTokenizer(text, " \t\n\r\f", null);
	 *
	 * @param text a string to be parsed.
	 * @throws NullPointerException if text is null.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public StringTokenizer(String text){
		this(text, " \t\n\r\f", null);
	}

	/**
	 * Set the text to be tokenized in this StringTokenizer.
	 * <p>
	 * This is useful when for StringTokenizer re-use so that new string tokenizers do no
	 * have to be created for each string you want to tokenizer.
	 * <p>
	 * The string will be tokenized from the beginning of the string.
	 *
	 * @param text a string to be parsed.
	 * @throws NullPointerException if text is null.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public void setText(String text){
		if (text == null){
			throw new NullPointerException();
		}
		this.text = text;
		strLength = text.length();
		emptyReturned = false;
		// set the position to start evaluation to zero
		// unless the string has no length, in which case
		// the entire string has already been examined.
		position = (strLength > 0 ? 0: -1);
		// because the text was changed since the last time the delimiters
		// were changed we need to set the delimiter changed position
		delimsChangedPosition = 0;
		// The token count changes when the text changes
		tokenCount = -1;
	}

	/**
	 * Set the delimiters for this StringTokenizer.
	 * The position must be initialized before this method is used.
	 * (setText does this and it is called from the constructor)
	 *
	 * @param nontokenDelims delimiters that should not be returned as tokens.
	 * @param tokenDelims delimiters that should be returned as tokens.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	private void setDelims(String nontokenDelims, String tokenDelims){
		this.nontokenDelims = nontokenDelims;
		this.tokenDelims = tokenDelims;
		// If we change delimiters, we do not want to start fresh,
		// without returning empty tokens.
		// the delimiter changed position can never be less than
		// zero, unlike position.
		delimsChangedPosition = (position != -1 ? position : strLength);
		// set the max delimiter
		maxDelimChar = 0;
		for (int i=0; nontokenDelims != null && i < nontokenDelims.length(); i++){
			if (maxDelimChar < nontokenDelims.charAt(i)){
				maxDelimChar = nontokenDelims.charAt(i);
			}
		}
		for (int i=0; tokenDelims != null && i < tokenDelims.length(); i++){
			if (maxDelimChar < tokenDelims.charAt(i)){
				maxDelimChar = tokenDelims.charAt(i);
			}
		}
		// Changing the delimiters may change the number of tokens
		tokenCount = -1;
	}


	/**
	 * Tests if there are more tokens available from this tokenizer's string.
	 * If this method returns <tt>true</tt>, then a subsequent call to
	 * <tt>nextToken</tt> with no argument will successfully return a token.
	 * <p>
	 * The current position is not changed.
	 *
	 * @return <code>true</code> if and only if there is at least one token in the
	 *          string after the current position; <code>false</code> otherwise.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public boolean hasMoreTokens(){

		// handle the easy case in which the number
		// of tokens has been counted.
		if (tokenCount == 0){
			return false;
		} else if (tokenCount > 0){
			return true;
		}

		// copy over state variables from the class to local
		// variables so that the state of this object can be
		// restored to the state that it was in before this
		// method was called.
		int savedPosition = position;
		boolean savedEmptyReturned = emptyReturned;

		int workingPosition = position;
		boolean workingEmptyReturned = emptyReturned;
		boolean onToken = advancePosition();
		while(position != workingPosition ||
			emptyReturned != workingEmptyReturned){
			if (onToken){
				// restore object state
				position = savedPosition;
				emptyReturned = savedEmptyReturned;
				return true;
			}
			workingPosition = position;
			workingEmptyReturned = emptyReturned;
			onToken = advancePosition();
		}

		// restore object state
		position = savedPosition;
		emptyReturned = savedEmptyReturned;
		return false;
	}

	/**
	 * Returns the next token from this string tokenizer.
	 * <p>
	 * The current position is set after the token returned.
	 *
	 * @return the next token from this string tokenizer.
	 * @throws NoSuchElementException if there are no more tokens in this tokenizer's string.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public String nextToken(){
		int workingPosition = position;
		boolean workingEmptyReturned = emptyReturned;
		boolean onToken = advancePosition();
		while(position != workingPosition ||
			emptyReturned != workingEmptyReturned){
			if (onToken){
				// returning a token decreases the token count
				tokenCount--;
				return (emptyReturned ? "" : text.substring(workingPosition, (position != -1) ? position : strLength));
			}
			workingPosition = position;
			workingEmptyReturned = emptyReturned;
			onToken = advancePosition();
		}
		throw new java.util.NoSuchElementException();
	}

	/**
	 * Advances the current position so it is before the next token.
	 * <p>
	 * This method skips nontoken delimiters but does not skip
	 * token delimiters.
	 * <p>
	 * This method is useful when switching to the new delimiter sets (see the
	 * second example in the class comment.)
	 *
	 * @return <code>true</code> if there are more tokens, <code>false</code> otherwise.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public boolean skipDelimiters(){
		int workingPosition = position;
		boolean workingEmptyReturned = emptyReturned;
		boolean onToken = advancePosition();

		// skipping delimiters may cause the number of tokens to change
		tokenCount = -1;

		while(position != workingPosition ||
			emptyReturned != workingEmptyReturned){
			if (onToken){
				// restore the state to just as it was before we found
				// this token and return
				position = workingPosition;
				emptyReturned = workingEmptyReturned;
				return true;
			}
			workingPosition = position;
			workingEmptyReturned = emptyReturned;
			onToken = advancePosition();
		}

		// the end of the string was reached
		// without finding any tokens
		return false;
	}

	/**
	 * Calculates the number of times that this tokenizer's <code>nextToken</code>
	 * method can be called before it generates an exception. The current position
	 * is not advanced.
	 *
	 * @return the number of tokens remaining in the string using the current
	 *    delimiter set.
	 *
	 * @see #nextToken()
	 * @since ostermillerutils 1.00.00
	 */
	public int countTokens(){

		// return the cached token count if a cache
		// is available.
		if (this.tokenCount >=0){
			return this.tokenCount;
		}

		int tokenCount = 0;

		// copy over state variables from the class to local
		// variables so that the state of this object can be
		// restored to the state that it was in before this
		// method was called.
		int savedPosition = position;
		boolean savedEmptyReturned = emptyReturned;

		int workingPosition = position;
		boolean workingEmptyReturned = emptyReturned;
		boolean onToken = advancePosition();
		while(position != workingPosition ||
			emptyReturned != workingEmptyReturned){
			if (onToken){
				tokenCount++;
			}
			workingPosition = position;
			workingEmptyReturned = emptyReturned;
			onToken = advancePosition();
		}

		// restore object state
		position = savedPosition;
		emptyReturned = savedEmptyReturned;

		// Save the token count in case this is called again
		// so we wouldn't have to do so much work.
		this.tokenCount = tokenCount;

		return tokenCount;
	}

	/**
	 * Set the delimiters used to this set of (nontoken) delimiters.
	 *
	 * @param delims the new set of nontoken delimiters (the set of token delimiters will be empty).
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public void setDelimiters(String delims){
		setDelims(delims, null);
	}

	/**
	 * Set the delimiters used to this set of delimiters.
	 *
	 * @param delims the new set of delimiters.
	 * @param delimsAreTokens flag indicating whether the first parameter specifies
	 *    token or nontoken delimiters: false -- the first parameter specifies nontoken
	 *    delimiters, the set of token delimiters is empty; true -- the first parameter
	 *    specifies token delimiters, the set of nontoken delimiters is empty.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public void setDelimiters(String delims, boolean delimsAreTokens){
		setDelims((delimsAreTokens ? null : delims), (delimsAreTokens ? delims : null));
	}

	/**
	 * Set the delimiters used to this set of delimiters.
	 *
	 * @param nontokenDelims the new set of nontoken delimiters.
	 * @param tokenDelims the new set of token delimiters.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public void setDelimiters(String nontokenDelims, String tokenDelims){
		setDelims(nontokenDelims, tokenDelims);
	}

	/**
	 * Set the delimiters used to this set of delimiters.
	 *
	 * @param nontokenDelims the new set of nontoken delimiters.
	 * @param tokenDelims the new set of token delimiters.
	 * @param returnEmptyTokens true if empty tokens may be returned; false otherwise.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public void setDelimiters(String nontokenDelims, String tokenDelims, boolean returnEmptyTokens){
		setDelims(nontokenDelims, tokenDelims);
		setReturnEmptyTokens(returnEmptyTokens);
	}

	/**
	 * Calculates the number of times that this tokenizer's <code>nextToken</code>
	 * method can be called before it generates an exception using the given set of
	 * (nontoken) delimiters.  The delimiters given will be used for future calls to
	 * nextToken() unless new delimiters are given. The current position
	 * is not advanced.
	 *
	 * @param delims the new set of nontoken delimiters (the set of token delimiters will be empty).
	 * @return the number of tokens remaining in the string using the new
	 *    delimiter set.
	 *
	 * @see #countTokens()
	 * @since ostermillerutils 1.00.00
	 */
	public int countTokens(String delims){
		setDelims(delims, null);
		return countTokens();
	}

	/**
	 * Calculates the number of times that this tokenizer's <code>nextToken</code>
	 * method can be called before it generates an exception using the given set of
	 * delimiters.  The delimiters given will be used for future calls to
	 * nextToken() unless new delimiters are given. The current position
	 * is not advanced.
	 *
	 * @param delims the new set of delimiters.
	 * @param delimsAreTokens flag indicating whether the first parameter specifies
	 *    token or nontoken delimiters: false -- the first parameter specifies nontoken
	 *    delimiters, the set of token delimiters is empty; true -- the first parameter
	 *    specifies token delimiters, the set of nontoken delimiters is empty.
	 * @return the number of tokens remaining in the string using the new
	 *    delimiter set.
	 *
	 * @see #countTokens()
	 * @since ostermillerutils 1.00.00
	 */
	public int countTokens(String delims, boolean delimsAreTokens){
		setDelims((delimsAreTokens ? null : delims), (delimsAreTokens ? delims : null));
		return countTokens();
	}

	/**
	 * Calculates the number of times that this tokenizer's <code>nextToken</code>
	 * method can be called before it generates an exception using the given set of
	 * delimiters.  The delimiters given will be used for future calls to
	 * nextToken() unless new delimiters are given. The current position
	 * is not advanced.
	 *
	 * @param nontokenDelims the new set of nontoken delimiters.
	 * @param tokenDelims the new set of token delimiters.
	 * @return the number of tokens remaining in the string using the new
	 *    delimiter set.
	 *
	 * @see #countTokens()
	 * @since ostermillerutils 1.00.00
	 */
	public int countTokens(String nontokenDelims, String tokenDelims){
		setDelims(nontokenDelims, tokenDelims);
		return countTokens();
	}

	/**
	 * Calculates the number of times that this tokenizer's <code>nextToken</code>
	 * method can be called before it generates an exception using the given set of
	 * delimiters.  The delimiters given will be used for future calls to
	 * nextToken() unless new delimiters are given. The current position
	 * is not advanced.
	 *
	 * @param nontokenDelims the new set of nontoken delimiters.
	 * @param tokenDelims the new set of token delimiters.
	 * @param returnEmptyTokens true if empty tokens may be returned; false otherwise.
	 * @return the number of tokens remaining in the string using the new
	 *    delimiter set.
	 *
	 * @see #countTokens()
	 * @since ostermillerutils 1.00.00
	 */
	public int countTokens(String nontokenDelims, String tokenDelims, boolean returnEmptyTokens){
		setDelims(nontokenDelims, tokenDelims);
		setReturnEmptyTokens(returnEmptyTokens);
		return countTokens();
	}

	/**
	 * Advances the state of the tokenizer to the next token or delimiter.  This method only
	 * modifies the class variables position, and emptyReturned.  The type of token that
	 * should be emitted can be deduced by examining the changes to these two variables.
	 * If there are no more tokens, the state of these variables does not change at all.
	 *
	 * @return true if we are at a juncture at which a token may be emitted, false otherwise.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	private boolean advancePosition(){
		// if we are returning empty tokens, we are just starting to tokenizer
		// and there is a delimiter at the beginning of the string or the string
		// is empty we need to indicate that there is an empty token at the beginning.
		// The beginning is defined as where the delimiters were last changed.
		if (returnEmptyTokens && !emptyReturned &&
			(delimsChangedPosition == position ||
			(position == -1 && strLength == delimsChangedPosition))){
			if (strLength == delimsChangedPosition){
				// Case in which the string (since delim change)
				// is empty, but because we are returning empty
				// tokens, a single empty token should be returned.
				emptyReturned = true;
				/*System.out.println("Empty token for empty string.");*/
				return true;
			} else {
				char c = text.charAt(position);
				if (c <= maxDelimChar &&
					(nontokenDelims != null && nontokenDelims.indexOf(c) != -1) ||
					(tokenDelims != null && tokenDelims.indexOf(c) != -1)){
					// There is delimiter at the very start of the string
					// so we must return an empty token at the beginning.
					emptyReturned = true;
					/*System.out.println("Empty token at beginning.");*/
					return true;
				}
			}
		}
		// The main loop
		// Do this as long as parts of the string have yet to be examined
		while (position != -1){
			char c = text.charAt(position);
			if (returnEmptyTokens && !emptyReturned && position > delimsChangedPosition){
				char c1 = text.charAt(position - 1);
				// Examine the current character and the one before it.
				// If both of them are delimiters, then we need to return
				// an empty delimiter.  Note that characters that were examined
				// before the delimiters changed should not be reexamined.
				if (c <= maxDelimChar && c1 <= maxDelimChar &&
					((nontokenDelims != null && nontokenDelims.indexOf(c) != -1) ||
					(tokenDelims != null && tokenDelims.indexOf(c) != -1)) &&
					((nontokenDelims != null && nontokenDelims.indexOf(c1) != -1) ||
					(tokenDelims != null && tokenDelims.indexOf(c1) != -1))){
					emptyReturned = true;
					/*System.out.println("Empty token.");*/
					return true;
				}
			}

			int nextDelimiter = (position < strLength - 1 ? indexOfNextDelimiter(position + 1) : -1);
			if (c > maxDelimChar ||
				((nontokenDelims == null || nontokenDelims.indexOf(c) == -1) &&
				(tokenDelims == null || tokenDelims.indexOf(c) == -1))){
				// token found
				/*System.out.println("Token: '" +
					text.substring(position, (nextDelimiter == -1 ? strLength : nextDelimiter)) +
					"' at " + position + ".");*/
				position = nextDelimiter;
				emptyReturned = false;
				return true;
			} else if (tokenDelims != null && tokenDelims.indexOf(c) != -1) {
				// delimiter that can be returned as a token found
				emptyReturned = false;
				/*System.out.println("Delimiter: '" + c + "' at " + position + ".");*/
				position = (position < strLength -1 ? position +1 : -1);
				return true;
			} else {
				// delimiter that is not a token found.
				emptyReturned = false;
				position = (position < strLength -1 ? position +1 : -1);
				return false;
			}
		}
		// handle the case that a token is at the end of the string and we should
		// return empty tokens.
		if (returnEmptyTokens && !emptyReturned && strLength > 0){
			char c = text.charAt(strLength - 1);
			if (c <= maxDelimChar &&
				(nontokenDelims != null && nontokenDelims.indexOf(c) != -1) ||
				(tokenDelims != null && tokenDelims.indexOf(c) != -1)){
				// empty token at the end of the string found.
				emptyReturned = true;
				/*System.out.println("Empty token at end.");*/
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the next token in this string tokenizer's string.
	 * <p>
	 * First, the sets of token and nontoken delimiters are changed to be the
	 * <code>tokenDelims</code> and <code>nontokenDelims</code>, respectively.
	 * Then the next token (with respect to new delimiters) in the string after the
	 * current position is returned.
	 * <p>
	 * The current position is set after the token returned.
	 * <p>
	 * The new delimiter sets remains the used ones after this call.
	 *
	 * @param nontokenDelims the new set of nontoken delimiters.
	 * @param tokenDelims the new set of token delimiters.
	 * @return the next token, after switching to the new delimiter set.
	 * @throws NoSuchElementException if there are no more tokens in this tokenizer's string.
	 * @see #nextToken()
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public String nextToken(String nontokenDelims, String tokenDelims){
		setDelims(nontokenDelims, tokenDelims);
		return nextToken();
	}

	/**
	 * Returns the next token in this string tokenizer's string.
	 * <p>
	 * First, the sets of token and nontoken delimiters are changed to be the
	 * <code>tokenDelims</code> and <code>nontokenDelims</code>, respectively;
	 * and whether or not to return empty tokens is set.
	 * Then the next token (with respect to new delimiters) in the string after the
	 * current position is returned.
	 * <p>
	 * The current position is set after the token returned.
	 * <p>
	 * The new delimiter set remains the one used for this call and empty tokens are
	 * returned in the future as they are in this call.
	 *
	 * @param nontokenDelims the new set of nontoken delimiters.
	 * @param tokenDelims the new set of token delimiters.
	 * @param returnEmptyTokens true if empty tokens may be returned; false otherwise.
	 * @return the next token, after switching to the new delimiter set.
	 * @throws NoSuchElementException if there are no more tokens in this tokenizer's string.
	 * @see #nextToken()
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public String nextToken(String nontokenDelims, String tokenDelims, boolean returnEmptyTokens){
		setDelims(nontokenDelims, tokenDelims);
		setReturnEmptyTokens(returnEmptyTokens);
		return nextToken();
	}

	/**
	 * Returns the next token in this string tokenizer's string.
	 * <p>
	 * Is equivalent to:
	 * <ul>
	 * <li> If the second parameter is <code>false</code> --
	 *      <code>nextToken(delims, null)</code>
	 * <li> If the second parameter is <code>true</code> --
	 *      <code>nextToken(null ,delims)</code>
	 * </ul>
	 * <p>
	 * @param delims the new set of token or nontoken delimiters.
	 * @param delimsAreTokens
	 *     flag indicating whether the first parameter specifies token or
	 *     nontoken delimiters: <code>false</code> -- the first parameter
	 *     specifies nontoken delimiters, the set of token delimiters is
	 *     empty; <code>true</code> -- the first parameter specifies token
	 *     delimiters, the set of nontoken delimiters is empty.
	 * @return the next token, after switching to the new delimiter set.
	 * @throws NoSuchElementException if there are no more tokens in this tokenizer's string.
	 *
	 * @see #nextToken(String,String)
	 * @since ostermillerutils 1.00.00
	 */
	public String nextToken(String delims, boolean delimsAreTokens){
		return (delimsAreTokens ? nextToken(null, delims) : nextToken(delims, null));
	}

	/**
	 * Returns the next token in this string tokenizer's string.
	 * <p>
	 * Is equivalent to <code>nextToken(delims, null)</code>.
	 *
	 * @param nontokenDelims the new set of nontoken delimiters (the set of
	 *     token delimiters will be empty).
	 * @return the next token, after switching to the new delimiter set.
	 * @throws NoSuchElementException if there are no more tokens in this
	 *     tokenizer's string.
	 *
	 * @see #nextToken(String,String)
	 * @since ostermillerutils 1.00.00
	 */
	public String nextToken(String nontokenDelims){
		return nextToken(nontokenDelims, null);
	}

	/**
	 * Similar to String.indexOf(int, String) but will look for
	 * any character from string rather than the entire string.
	 *
	 * @param start index in text at which to begin the search
	 * @return index of the first delimiter from the start index (inclusive), or -1
	 *     if there are no more delimiters in the string
	 *
	 * @since ostermillerutils 1.00.00
	 */
	private int indexOfNextDelimiter(int start){
		char c;
		int next;
		for (next = start; (c = text.charAt(next)) > maxDelimChar ||
			((nontokenDelims == null || nontokenDelims.indexOf(c) == -1) &&
			(tokenDelims == null || tokenDelims.indexOf(c) == -1)); next++){
			if (next == strLength - 1){
				// we have reached the end of the string without
				// finding a delimiter
				return (-1);
			}
		}
		return next;
	}

	/**
	 * Returns the same value as the <code>hasMoreTokens()</code> method. It exists
	 * so that this class can implement the <code>Enumeration</code> interface.
	 *
	 * @return <code>true</code> if there are more tokens;
	 *    <code>false</code> otherwise.
	 *
	 * @see java.util.Enumeration
	 * @see #hasMoreTokens()
	 * @since ostermillerutils 1.00.00
	 */
	public boolean hasMoreElements(){
		return hasMoreTokens();
	}

	/**
	 * Returns the same value as the <code>nextToken()</code> method, except that
	 * its declared return value is <code>Object</code> rather than
	 * <code>String</code>. It exists so that this class can implement the
	 * <code>Enumeration</code> interface.
	 *
	 * @return the next token in the string.
	 * @throws NoSuchElementException if there are no more tokens in this tokenizer's string.
	 *
	 * @see java.util.Enumeration
	 * @see #nextToken()
	 * @since ostermillerutils 1.00.00
	 */
	public Object nextElement(){
		return nextToken();
	}

	/**
	 * Returns the same value as the <code>hasMoreTokens()</code> method. It exists
	 * so that this class can implement the <code>Iterator</code> interface.
	 *
	 * @return <code>true</code> if there are more tokens;
	 *     <code>false</code> otherwise.
	 *
	 * @see java.util.Iterator
	 * @see #hasMoreTokens()
	 * @since ostermillerutils 1.00.00
	 */
	public boolean hasNext(){
		return hasMoreTokens();
	}

	/**
	 * Returns the same value as the <code>nextToken()</code> method, except that
	 * its declared return value is <code>Object</code> rather than
	 * <code>String</code>. It exists so that this class can implement the
	 * <code>Iterator</code> interface.
	 *
	 * @return the next token in the string.
	 * @throws NoSuchElementException if there are no more tokens in this tokenizer's string.
	 *
	 * @see java.util.Iterator
	 * @see #nextToken()
	 * @since ostermillerutils 1.00.00
	 */
	public Object next(){
		return nextToken();
	}

	/**
	 * This implementation always throws <code>UnsupportedOperationException</code>.
	 * It exists so that this class can implement the <code>Iterator</code> interface.
	 *
	 * @throws UnsupportedOperationException always is thrown.
	 *
	 * @see java.util.Iterator
	 * @since ostermillerutils 1.00.00
	 */
	public void remove(){
		throw new UnsupportedOperationException();
	}

	/**
	 * Set whether empty tokens should be returned from this point in
	 * in the tokenizing process onward.
	 * <P>
	 * Empty tokens occur when two delimiters are next to each other
	 * or a delimiter occurs at the beginning or end of a string. If
	 * empty tokens are set to be returned, and a comma is the non token
	 * delimiter, the following table shows how many tokens are in each
	 * string.<br>
	 * <table><tr><th>String<th><th>Number of tokens<th></tr>
	 * <tr><td align=right>"one,two"<td><td>2 - normal case with no empty tokens.<td></tr>
	 * <tr><td align=right>"one,,three"<td><td>3 including the empty token in the middle.<td></tr>
	 * <tr><td align=right>"one,"<td><td>2 including the empty token at the end.<td></tr>
	 * <tr><td align=right>",two"<td><td>2 including the empty token at the beginning.<td></tr>
	 * <tr><td align=right>","<td><td>2 including the empty tokens at the beginning and the ends.<td></tr>
	 * <tr><td align=right>""<td><td>1 - all strings will have at least one token if empty tokens are returned.<td></tr></table>
	 *
	 * @param returnEmptyTokens true iff empty tokens should be returned.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public void setReturnEmptyTokens(boolean returnEmptyTokens){
		// this could effect the number of tokens
		tokenCount = -1;
		this.returnEmptyTokens = returnEmptyTokens;
	}

	/**
	 * Get the the index of the character immediately
	 * following the end of the last token.  This is the position at which this tokenizer will begin looking
	 * for the next token when a <code>nextToken()</code> method is invoked.
	 *
	 * @return the current position or -1 if the entire string has been tokenized.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public int getCurrentPosition(){
		return this.position;
	}

	/**
	 * Retrieve all of the remaining tokens in a String array.
	 * This method uses the options that are currently set for
	 * the tokenizer and will advance the state of the tokenizer
	 * such that <code>hasMoreTokens()</code> will return false.
	 *
	 * @return an array of tokens from this tokenizer.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public String[] toArray(){
		String[] tokenArray = new String[countTokens()];
		for(int i=0; hasMoreTokens(); i++) {
			tokenArray[i] = nextToken();
		}
		return tokenArray;
	}

	/**
	 * Retrieves the rest of the text as a single token.
	 * After calling this method hasMoreTokens() will always return false.
	 *
	 * @return any part of the text that has not yet been tokenized.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public String restOfText(){
		return nextToken(null, null);
	}

	/**
	 * Returns the same value as nextToken() but does not alter
	 * the internal state of the Tokenizer.  Subsequent calls
	 * to peek() or a call to nextToken() will return the same
	 * token again.
	 *
	 * @return the next token from this string tokenizer.
	 * @throws NoSuchElementException if there are no more tokens in this tokenizer's string.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public String peek(){
		// copy over state variables from the class to local
		// variables so that the state of this object can be
		// restored to the state that it was in before this
		// method was called.
		int savedPosition = position;
		boolean savedEmptyReturned = emptyReturned;
		int savedtokenCount = tokenCount;

		// get the next token
		String retval = nextToken();

		// restore the state
		position = savedPosition;
		emptyReturned = savedEmptyReturned;
		tokenCount = savedtokenCount;

		// return the nextToken;
		return(retval);
	}
}

class UnsupportedOperationException extends RuntimeException {

}