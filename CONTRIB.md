# deegree Contribution Guidelines

Contributing to open source can be a rewarding way to learn, teach, and build experience in just about any skill you can imagine.

## What do I need to know to help?

If you are looking to help to with a code contribution our project uses the Java programming language and a couple of 
open source frameworks. If you don't feel ready to make a code contribution yet, no problem! 
You can also check out the [documentation issues](https://github.com/deegree/deegree3/issues?utf8=%E2%9C%93&q=is%3Aissue+is%3Aopen+label%3Adocumentation) 
or the [infrastructure issues](https://github.com/deegree/deegree3/issues?utf8=%E2%9C%93&q=is%3Aissue+is%3Aopen+label%3Awebsite) that we have.

If you are interested in making a code contribution and would like to learn more about the technologies that we use, check out the list below.

## How do I make a contribution?

Never made an open source contribution before? Wondering how contributions work in the in our project? Here's a quick rundown!

- Find an issue that you are interested in addressing or a feature that you would like to add.
- Fork the repository associated with the issue to your local GitHub organization. This means that you will have a copy of the repository under your-GitHub-username/repository-name.
- Clone the repository to your local machine using git clone https://github.com/github-username/repository-name.git`.
- Create a new branch for your fix using `git checkout -b branch-name-here`.
- Make the appropriate changes for the issue you are trying to address or the feature that you want to add.
- Use git add insert-paths-of-changed-files-here to add the file contents of the changed files to the "snapshot" git uses to manage the state of the project, also known as the index.
- Use git commit -m "Insert a short message of the changes made here" to store the contents of the index with a descriptive message.
- Push the changes to the remote repository using `git push origin branch-name-here`.
- Submit a pull request to the upstream repository.
- Title the pull request with a short description of the changes made and the issue or bug number associated with your change. For example, you can title an issue like so "Fixed #4352 to return valid GML".
- In the description of the pull request, explain the changes that you made, any issues you think exist with the pull request you made, and any questions you have for the maintainer. It's OK if your pull request is not perfect (no pull request is), the reviewer will be able to help you fix any problems and improve it!
- Wait for the pull request to be reviewed by a TMC member.
- Make changes to the pull request if the reviewing maintainer recommends them.
- Celebrate your success after your pull request is merged and post it to twitter @deegree_org and #deegree!

Further information:
- https://deegree.org/community/guidelines
- https://github.com/deegree/deegree3/wiki/Bug-or-Feature
- https://github.com/deegree/deegree3/wiki/Developer-Guidelines

## Where can I go for help?

If you need help, you can ask questions on our mailing list, IRC chat, or file an [issue](../issues).

### Getting in contact

#### Mailing Lists

- Users:
questions regarding installing and deploying deegree
questions on service configuration

- Developers:
to add code, supply patches etc
discuss future code developments

#### Issue Tracker
Our issue tracking system is:

- [github issue tracker](../issues) (please use this one for all inquiries!)

#### Contact PSC & TMC
PSC <psc AT SPAMFREE deegree DOT org> (Who is the ProjectSteeringCommittee and what do they do?)
TMC <tmc AT SPAMFREE deegree DOT org> (Who is the TechnicalManagementCommittee and what do they do?)

#### IRC Channel
Channel #deegree on freenode.net (irc://freenode.net). Choose an IRC client for your platform or use the webclient for connecting:

Open the freenode web client or enter /network freenode in your chat client
- Choose a nickname
- Enter channel #deegree or type /join #deegree in your chat client
- Solve captcha
- Connect

### Structures and Procedures of the deegree project

#### Overview
Within the deegree project the following kinds of contribution are recognized:

- Users are individuals or institutions who download, install and use deegree components.
- Developers are individuals or institutions who are able to apply changes to the deegree source code.
- Committers are individuals from the Users or Developers groups who are allowed to commit artefacts to the deegree code repository, where Users typically contribute to documentation or configuration and Developers commit source code.
- The Technical Management Committee is a small group of individuals who are responsible for all technical aspects of project management.
- The Project Steering Committee is a small group of individuals who are responsible for all organisational and strategic aspects of project management.

The remainder of this document contains more detailed information about these groups or structural entities.

#### Users
Users are individuals or institutions who download, install and use deegree components.

Their main contribution to the community is testing and evaluation of new releases, features, and documentation. Users provide feedback by using the deegree-users mailing list, which might also imply requests for new features or enhancements. Further more, they are the main group to develop and enhance the documentation and participate in any outreach activities to make deegree better known in the geospatial information community and beyond.

#### Developers
Developers are individuals or institutions who are able to apply changes to the deegree source code.

Their software engineering point of view focusses on technical aspects of the deegree software. A developer's contribution to the deegree community may constist of testing and evaluation of new releases of features on a technical level, i.e. source code or software design perspective. They may contribute bug fixes or newly developped features to the community, make announcements about their work on the deegree-devel or deegree-users mailing lists and may actively participate in technical discussions.

#### Committers
Committers are individuals from the Users or Developers groups who are allowed to commit artefacts to the deegree code repository, where Users typically contribute to documentation or configuration and Developers commit source code.

Commit rights are granted upon request by the PSC. Individuals who already have contributed significant resources to the project may apply for commit rights. The prior commitment is usually shown by regularly submitting code that conforms to the current deegree developer guide and by involvement into the discussions at the developer and user list.

Any individual that has been accepted the legal statement can contribute.

#### Technical Management Committee
The Technical Management Committee is a small group of individuals who are responsible for all technical aspects of project management.

Responsibilities of the TMC include:

- deciding about integration of feature additions or bug fixes provided by community members
- ensuring regular releases (major and maintenance) of deegree software
- developing technical standards and policies (e.g. coding standards, file naming conventions, etc...)
- reviewing feature requests for technical enhancements to the software
- project infrastructure (e.g. build server, artifact repository, etc. For information about infrastructure components see the [wiki page](https://github.com/deegree/deegree3/wiki/Infrastructure))

Membership: TMC members are:

- Reijer Copier (IDgis)
- Torsten Friebe (lat/lon)
- Stephan Reichhelm (grit)

Contact: You may contact the TMC via email: <tmc AT SPAMFREE deegree DOT org>.

#### Project Steering Committee

The Project Steering Committee is a small group of individuals who are responsible for all organisational and strategic aspects of project management. The PSC decides about the major directions of the deegree project, the dates and content of major releases and who the project developers are. All decisions made by the PSC are either made in consensus or based on a single majority. The PSC communicates via E-Mail, IRC (#deegree) and usually meets once a year in person.

Current PSC Members are:

- Herman Assink (IDgis)
- Jens Fitzke (lat/lon), Chair
- Prof. Dr. Klaus Greve (Bonn University)

Contact: You may contact the PSC via email: <psc AT SPAMFREE deegree DOT org>. There is also an issue tracker for the PSC, for the organisational side of the deegree project.

Responsibilities of the PSC include:

- formalization of affiliation with external entities such as OSGeo
- project sponsorship

Membership: The PSC is made up of individuals consisting of Committers and prominent members of the deegree user community. There is no set number of members for the PSC although the initial desire is to set the membership at a maximum of 10.

#### Adding Members

Any member of the deegree-devel mailing list may nominate someone for committee membership at any time. Only existing committee members may vote on new members. Nominees must receive a majority vote from existing members to be added to the committee.

#### Stepping Down

If for any reason a committee member is not able to fully participate then they certainly are free to step down. If a member is not active (e.g. no voting, no email participation) for a period of two months then the committee reserves the right to seek nominations to fill that position. Should that person become active again (hey, it happens) then they would certainly be welcome, but would require a nomination.


### Access to technical infrastructure components

The technical infrastructure of the deegree initiative is described in [Infrastructure](https://github.com/deegree/deegree3/wiki/Infrastructure).

If a member of the initiative needs access to one of the infrastructure components, the TMC decides upon tracker request and updates the respective list in Infrastructure.

#### Ownership of github organisation and projects 

Only committee members have full access to the deegree organisation https://github.com/deegree and any github project under that organisation.
Permission for access shall be provided to new members only if accepted by one of the deegree Committees. 
A proposal should be written to the mailing list and voted on normally. Sending the proposal to deegree-devel is sufficient.
Removal of access should be handled by the same process.

# Legal

Committers are the front line gatekeepers to keep the code base clear of improperly contributed code. 
It is important to the deegree users, developers and the OSGeo foundation to avoid contributing any code to the project
 without it being clearly licensed under the project license.

Generally speaking the key issues are that those providing code to be included in the repository understand that the 
code will be released under the LGPL license, and that the person providing the code has the right to contribute the 
code. For the committer themselves understanding about the license is hopefully clear. For other contributors, the 
commiter should verify the understanding unless the committer is very comfortable that the contributor understands the 
license (for instance frequent contributors).

All committers are responsible for having read, and understood this document.