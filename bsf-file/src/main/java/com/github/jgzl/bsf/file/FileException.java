package com.github.jgzl.bsf.file;


import com.github.jgzl.bsf.core.base.BsfException;

/**
 * @author Huang Zhaoping
 */
public class FileException extends BsfException {

	private static final long serialVersionUID = 1L;

	public FileException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileException(String message) {
        super(message);
    }
}
