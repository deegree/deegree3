# deegree Contribution Guidelines

Contributing to open source can be a rewarding way to learn, and build experience in just about any skill you can imagine.
Before you contribute to the OSGeo project deegree please read our guidelines we'd like you to follow:

* [OSGeo Code of Conduct](https://www.osgeo.org/code_of_conduct/)
* [How to help](#what-do-i-need-to-know-to-help?)
* [How to contribute](#how-do-i-make-a-contribution?)
* [Getting in contact](#getting-in-contact)
* [Structures and Procedures](#structures-and-procedures-of-the-deegree-project)
* [Legal](#legal) 

## What do I need to know to help?

If you are looking to help to with a code contribution our project uses the Java programming language and a couple of 
open source frameworks, see [Technology](https://www.deegree.org/about-deegree#technology) for more information. If you don't feel ready to make a code contribution yet, no problem! 
You can also check out the [documentation issues](https://github.com/deegree/deegree3/issues?utf8=%E2%9C%93&q=is%3Aissue+is%3Aopen+label%3Adocumentation) 
or the [infrastructure issues](https://github.com/deegree/deegree3/issues?utf8=%E2%9C%93&q=is%3Aissue+is%3Aopen+label%3Awebsite) that we have.

If you are interested in making a code contribution and would like to learn more about the technologies that we use, check out the list below.

## How do I make a contribution?

Never made an open source contribution before? Wondering how contributions work in the in our project? Here's a quick rundown!

- Find an issue that you are interested in addressing or a feature that you would like to add.
- Fork the repository associated with the issue to your GitHub organization or user account. This means that you will have a copy of the repository under `your-github-username/deegree3`.
- Clone the repository to your local machine using `git clone https://github.com/your-github-username/deegree3.git`.
- Create a new branch for your change using `git checkout -b your-branch-name-here`.
- Make the appropriate changes for the issue you are trying to address or the feature that you want to add.
- Use `git add insert-paths-of-changed-files-here` to add the file contents of the changed files to the "snapshot" git uses to manage the state of the project, also known as the index.
- Use `git commit -m "Insert a short message of the changes made here with reference to github issue"` to store the contents of the index with a descriptive message.
- Push the changes to the remote repository using `git push origin your-branch-name-here`.
- Submit a pull request to the upstream repository using the github functionality.
- Title the pull request with a short description of the changes made and the issue or bug number associated with your change. For example, you can title an issue like so "Fixes #42: WFS returns valid GML for GetFeature with BBOX parameter".
- In the description of the pull request, explain the changes that you made, any issues you think exist with the pull request you made, and any questions you have for the maintainer. It's OK if your pull request is not perfect (no pull request is), the reviewer will be able to help you fix any problems and improve it!
- Wait for the pull request to be reviewed by a TMC member.
- Make changes to the pull request if the reviewing maintainer recommends them. 
- Join the TMC meetings when your pull request will be voted on. 
- Celebrate your success after your pull request is merged and share it on twitter.

Further reading:
- https://deegree.org/community/guidelines
- https://github.com/deegree/deegree3/wiki/Bug-or-Feature
- https://github.com/deegree/deegree3/wiki/Developer-Guidelines
- https://github.com/deegree/deegree3/wiki/Working-with-pull-requests
- https://github.com/deegree/deegree3/wiki/Setting-up-deegree3-in-your-IDE

## Getting in contact

If you need help, you can ask questions on our mailing lists, IRC chat, [GIS stackexchange](https://gis.stackexchange.com/questions/tagged/deegree) or file an issue on github.

### Mailing Lists

- [Users](https://sourceforge.net/p/deegree/mailman/deegree-users/):
questions regarding installing and deploying deegree
questions on service configuration

- [Developers](https://sourceforge.net/p/deegree/mailman/deegree-devel/):
to add code, supply patches etc
discuss future code developments

### Issue Tracker
Our issue tracking system is:

- [github issue tracker](../../issues) (**please use this one for all inquiries!**)

### Contact PSC & TMC
- PSC `psc AT SPAMFREE deegree DOT org` (Who is the [ProjectSteeringCommittee](#project-steering-committee) and what do they do?)
- TMC `tmc AT SPAMFREE deegree DOT org` (Who is the [TechnicalManagementCommittee](#technical-management-committee) and what do they do?)

### IRC Channel
Channel #deegree on libera.chat (irc://irc.libera.chat/deegree). Choose an IRC client for your platform or use the webclient for connecting:

Open the web client at https://web.libera.chat/#deegree
- Choose a nickname
- Enter channel #deegree or type `/join #deegree` in your chat client
- Connect

### Social Media

At twitter we are [@deegree_org](https://twitter.com/deegree_org) and use `#OSGeo #deegree` for our tweets. 

## Structures and Procedures of the deegree project

### Overview
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
Committers are individuals from the Users or Developers groups who contribute to the deegree code repository, where Users typically contribute to documentation or configuration and Developers commit source code.

Any individual that has been accepted the [legal statement](#legal) below can contribute.

Overview of current [Contributors at github](https://github.com/deegree/deegree3/graphs/contributors).

### Technical Management Committee
The Technical Management Committee is a small group of individuals who are responsible for all technical aspects of project management.

Responsibilities of the TMC include:

- deciding about integration of feature additions or bug fixes provided by community members
- ensuring regular releases (major and maintenance) of the deegree software
- developing technical standards and policies (e.g. coding standards, file naming conventions, etc...)
- reviewing feature requests for technical enhancements to the deegree software
- project infrastructure (e.g. build server, artifact repository, etc. For information about infrastructure components see the [wiki page](https://github.com/deegree/deegree3/wiki/Infrastructure))

Membership: TMC members are:

- Reijer Copier (IDgis)
- Torsten Friebe (lat/lon)
- Stephan Reichhelm (grit)

Contact: You may contact the TMC via email: `tmc AT SPAMFREE deegree DOT org`.

### Project Steering Committee

The Project Steering Committee is a small group of individuals who are responsible for all organisational and strategic aspects of project management. The PSC decides about the major directions of the deegree project. All decisions made by the PSC are either made in consensus or based on a single majority. The PSC communicates via E-Mail, IRC (#deegree) and usually meets once a year in person.

Current PSC Members are:

- Herman Assink (IDgis)
- Jens Fitzke (lat/lon), Chair
- Prof. Dr. Klaus Greve (Bonn University)

Contact: You may contact the PSC via email: `psc AT SPAMFREE deegree DOT org`.

Responsibilities of the PSC include:

- formalization of affiliation with external entities such as OSGeo
- project sponsorship
- legal aspects

Membership: The PSC is made up of individuals consisting of Committers and prominent members of the deegree user community. There is no set number of members for the PSC although the initial desire is to set the membership at a maximum of 10.

### Adding Members

Any member of the deegree-devel mailing list may nominate someone for committee membership at any time. Only existing committee members may vote on new members. Nominees must receive a majority vote from existing members to be added to the committee.

### Stepping Down

If for any reason a committee member is not able to fully participate then they certainly are free to step down. If a member is not active (e.g. no voting, no email participation) for a period of two months then the committee reserves the right to seek nominations to fill that position. Should that person become active again (hey, it happens) then they would certainly be welcome, but would require a nomination.

### Access to technical infrastructure components

The technical infrastructure of the deegree initiative is described in [Infrastructure](https://github.com/deegree/deegree3/wiki/Infrastructure).

If a member of the initiative needs access to one of the infrastructure components, the TMC decides upon request and provides access to the infrastructure.

#### Ownership of github organisation and projects 

Only committee members have full access to the deegree organisation https://github.com/deegree and any github project under that organisation.
Permission for access shall be provided to new members only if accepted by one of the deegree Committees. 
A proposal should be written to the mailing list and voted on normally. Sending the proposal to deegree-devel is sufficient.
Removal of access should be handled by the same process.

## Legal

Committers are the front line gatekeepers to keep the code base clear of improperly contributed code. 
It is important to the deegree users, developers and the OSGeo foundation to avoid contributing any code to the project
 without it being clearly licensed under the project license.

Generally speaking the key issues are that those providing code to be included in the repository understand that the 
code will be released under the LGPL license, and that the person providing the code has the right to contribute the 
code. For the committer themselves understanding about the license is hopefully clear. For other contributors, the 
committer should verify the understanding unless the committer is very comfortable that the contributor understands the 
license (for instance frequent contributors).

If the contribution was developed on behalf of an employer (on work time, as part of a work project, etc) then it is 
important that an appropriate representative of the employer understand that the code will be contributed under the 
LGPL license. The arrangement should be cleared with an authorized supervisor, manager, or director.

The code should be developed by the contributor, or the code should be from a source which can be rightfully contributed
such as from the public domain, or from an open source project under a compatible license. All unusual situations need 
to be discussed with the committee members and documented.

Committers should adhere to the following guidelines, and may be personally legally liable for improperly contributing 
code to the source repository:

- Make sure the contributor (and possibly employer) is aware of the contribution terms.
- Code coming from a source other than the contributor (such as adapted from another project) should be clearly marked 
as to the original source, copyright holders, license terms and so forth. This information can be in the file headers, 
but should also be added to the project licensing file if not exactly matching normal project licensing (LICENSE.txt).
- Existing copyright headers and license text should never be stripped from a file. If a copyright holder wishes to give
 up copyright they must do so in writing to the committees before copyright messages are removed. If license terms are 
 changed it has to be by agreement (written in email is ok) of the copyright holders.
- Code with licenses requiring credit, or disclosure to users should be added to LICENSE.TXT.
- When substantial contributions are added to a file (such as substantial patches) the author/contributor should be added
 to the list of copyright holders for the file.
- If there is uncertainty about whether a change it proper to contribute to the code base, please seek more information 
from the project steering committee, or the OSGeo foundation legal counsel.

All committers are responsible for having read, and understood this document. And confirm acceptance to these 
guidelines by sending an email to the mailing list with subject `Committer guidelines acceptance` and body 
`I hereby accept the deegree committer guidelines. <Date> <Your Full Name>`.

