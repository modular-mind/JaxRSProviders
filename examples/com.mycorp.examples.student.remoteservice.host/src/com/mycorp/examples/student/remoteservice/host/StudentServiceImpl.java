/*******************************************************************************
* Copyright (c) 2015 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package com.mycorp.examples.student.remoteservice.host;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.ecf.osgi.services.remoteserviceadmin.DebugRemoteServiceAdminListener;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdminListener;

import com.mycorp.examples.student.Address;
import com.mycorp.examples.student.Student;
import com.mycorp.examples.student.StudentService;

// The jax-rs path annotation for this service
@Path("/studentservice")
// The OSGi DS (declarative services) component annotation. Note that the
// /rsexport.properties file defines this service impl as a remote service
// and configures the usage of the Jersey Jax-RS implementation as the
// desired distribution provider. See /rsexport.properties
@Component(immediate = true, properties = "rsexport.properties")
public class StudentServiceImpl implements StudentService {

	void activate(BundleContext context) throws Exception {
		// Setup debug output
		context.registerService(RemoteServiceAdminListener.class, new DebugRemoteServiceAdminListener(), null);
	}

	// Provide a map-based storage of students
	private static Map<String, Student> students = Collections.synchronizedMap(new HashMap<String, Student>());
	// Create a single student and add to students map
	static {
		Student s = new Student("Joe Senior");
		s.setId(UUID.randomUUID().toString());
		s.setGrade("First");
		Address a = new Address();
		a.setCity("New York");
		a.setState("NY");
		a.setPostalCode("11111");
		a.setStreet("111 Park Ave");
		s.setAddress(a);
		students.put(s.getId(), s);
	}

	// Implementation of StudentService based upon the students map
	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("/students")
	public List<Student> getStudents() {
		return new ArrayList<Student>(students.values());
	}

	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("/students/{studentId}")
	public Student getStudent(@PathParam("studentId") String id) {
		return students.get(id);
	}

	@POST
	@Produces(MediaType.APPLICATION_XML)
	@Path("/students/{studentName}")
	public Student createStudent(@PathParam("studentName") String studentName) {
		if (studentName == null)
			return null;
		synchronized (students) {
			Student s = new Student(studentName);
			s.setId(UUID.randomUUID().toString());
			students.put(s.getId(), s);
			return s;
		}
	}

	@PUT
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	@Path("/students")
	public Student updateStudent(Student student) {
		Student result = null;
		if (student != null) {
			String id = student.getId();
			if (id != null) {
				synchronized (students) {
					result = students.get(student.getId());
					if (result != null) {
						String newName = student.getName();
						if (newName != null)
							result.setName(newName);
						result.setGrade(student.getGrade());
						result.setAddress(student.getAddress());
					}
				}
			}
		}
		return result;
	}

	@DELETE
	@Path("/students/{studentId}")
	@Produces(MediaType.APPLICATION_XML)
	public Student deleteStudent(@PathParam("studentId") String studentId) {
		return students.remove(studentId);
	}
}
