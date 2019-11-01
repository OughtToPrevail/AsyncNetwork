/*
Copyright 2019 https://github.com/OughtToPrevail

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package oughttoprevail.asyncnetwork.packet.read;

class NamedSection
{
	private final String name;
	private final int beginIndex;
	private int endIndex;
	
	NamedSection(String name, int beginIndex)
	{
		this.name = name;
		this.beginIndex = beginIndex;
	}
	
	int getBeginIndex()
	{
		return beginIndex;
	}
	
	void setEndIndex(int endIndex)
	{
		this.endIndex = endIndex;
	}
	
	int getEndIndex()
	{
		return endIndex;
	}
	
	public String getName()
	{
		return name;
	}
}