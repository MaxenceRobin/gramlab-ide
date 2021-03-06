/*
 * Unitex
 *
 * Copyright (C) 2001-2018 Université Paris-Est Marne-la-Vallée <unitex@univ-mlv.fr>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA.
 *
 */
package fr.umlv.unitex.process.commands;

import java.io.File;

import fr.umlv.unitex.files.FileUtil;

/**
 * @author Sébastien Paumier
 */
public class CpCommand extends AbstractMethodCommand {

	private File src;
	private File dst;

	public CpCommand() {
		super("cp");
	}

	public CpCommand copy(File s, File d) {
		this.src = s;
		this.dst = d;
		protectElement(src.getAbsolutePath());
		protectElement(dst.getAbsolutePath());
		return this;
	}

	@Override
	public boolean execute() {
		return FileUtil.copyFile(src, dst);
	}

}
