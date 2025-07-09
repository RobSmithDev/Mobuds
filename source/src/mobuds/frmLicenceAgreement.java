/*
 * frmLicenceAgreement.java
 *
 * Created on 03 January 2009, 13:50
 *
 * Runs the licence agreement form
 */

package mobuds;

/**
 *
 * @author rob smith
 */
public class frmLicenceAgreement extends ModalForm {

    // Append a section onto the licence agreement
    private void addAgreement(String header, String text) {
        startNewSection(header);
        wordWrapLabels(text);
    }

    // Creates an instance of th elicence agreement
    public frmLicenceAgreement(MobudsSystem system, String title) {
        super(system,title);

        // OK and Cancel buttons
        addModalCommand("QUIT",system.imgCross,MR_CANCEL,CMD_BACK);
        addModalCommand("I ACCEPT",system.imgTick,MR_OK,CMD_NONE);

        addAgreement("LICENCE AGREEMENT", "THIS IS A LEGAL AGREEMENT between \"you\", and Crave Create, \"The Company\".\r\n\r\nUse of the software you are about to use indicates your acceptance of these terms.  As used in this Agreement, the capitalized term \"Software\" means the Mobuds software, in any form. \r\n\r\nIF YOU DO NOT AGREE TO THESE TERMS AND CONDITIONS, YOU SHOULD INDICATE BY PRESSING \"QUIT\" NOW");
        addAgreement("AGE", "The Software may only be used by persons aged "+MobudsProfile.MINIMUM_AGE+" or older.  IF YOU DO NOT AGREE TO THIS THEN YOU SHOULD SO INDICATE BY PRESSING \"QUIT\" NOW.");
        addAgreement("OUTLINE", "This Software will allow others around you to view selected personal information about you. By accepting this agreement you consent to this information being made visible to anyone while the Software is running.  IF YOU DO NOT WISH YOUR INFORMATION TO BE SEEN BY OTHERS OR ARE CONCERNED ABOUT PRIVACY YOU SHOULD SO INDICATE THIS BY PRESSING \"QUIT\" NOW.");
        addAgreement("Upgrades, Updates And Enhancements", "All upgrades, updates or enhancements of the Software shall be deemed to be part of the Software and will be subject to this Agreement.");
        addAgreement("Prohibited Conduct", "You represent and warrant that you will not violate any of the terms and conditions set forth in this Agreement and that:\nA. You will not, and will not permit others to: (i) reverse engineer, decompile, disassemble, derive the source code of, modify, or create derivative works from the Software; or (ii) use, copy, modify, alter, or transfer, electronically or otherwise, the Software or any of the accompanying documentation except as expressly permitted in this Agreement; or (iii) redistribute, sell, rent, lease, sublicense, or otherwise transfer rights to the Software whether in a stand-alone configuration or as incorporated with other software code written by any party except as expressly permitted in this Agreement.\nB. You will not use the Software to engage in or allow others to engage in any illegal activity.\nC. You will not engage in use of the Software that will interfere with or damage the operation of the services of third parties by overburdening/disabling network resources through automated queries, excessive usage or similar conduct.\nD. You will not use the Software to engage in any activity that will violate the rights of third parties, including, without limitation, through the use, public display, public performance, reproduction, distribution, or modification of communications or materials that infringe copyrights, trademarks, publicity rights, privacy rights, other proprietary rights, or rights against defamation of third parties.\nE. You will not transfer the Software or utilize the Software in combination with third party software authored by you or others to create an integrated software program which you transfer to unrelated third parties.");
        addAgreement("Disclaimer of Warranty", "THE SOFTWARE IS PROVIDED ON AN \u201CAS IS\u201D BASIS, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING, WITHOUT LIMITATION, THE WARRANTIES THAT IT IS FREE OF DEFECTS, VIRUS FREE, ABLE TO OPERATE ON AN UNINTERRUPTED BASIS, MERCHANTABLE, FIT FOR A PARTICULAR PURPOSE OR NON-INFRINGING.  THIS DISCLAIMER OF WARRANTY CONSTITUTES AN ESSENTIAL PART OF THIS LICENSE AND AGREEMENT.  NO USE OF THE SOFTWARE IS AUTHORIZED HEREUNDER EXCEPT UNDER THIS DISCLAIMER.");
        addAgreement("Limitation of Liability", "TO THE MAXIMUM EXTENT PERMITTED BY APPLICABLE LAW, IN NO EVENT WILL THE COMPANY BE LIABLE FOR ANY INDIRECT, SPECIAL, INCIDENTAL OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, INCLUDING, WITHOUT LIMITATION, DAMAGES FOR LOST PROFITS, LOSS OF GOODWILL, WORK STOPPAGE, COMPUTER FAILURE OR MALFUNCTION, OR ANY AND ALL OTHER COMMERCIAL DAMAGES OR LOSSES, EVEN IF ADVISED OF THE POSSIBILITY THEREOF, AND REGARDLESS OF THE LEGAL OR EQUITABLE THEORY (CONTRACT, TORT OR OTHERWISE) UPON WHICH THE CLAIM IS BASED.  IN ANY CASE, THE COMPANY\u2019S COLLECTIVE LIABILITY UNDER ANY PROVISION OF THIS LICENSE SHALL NOT EXCEED IN THE AGGREGATE THE SUM OF THE FEES (IF ANY) YOU PAID \nFOR THIS LICENSE.");
        addAgreement("Legends and Notices", "You agree that you will not remove or alter any trademark, logo, copyright or other proprietary notices, legends, symbols or labels in the Software or any accompanying documentation.");
        addAgreement("Term and Termination", "This Agreement is effective upon your acceptance as provided herein and payment of the applicable license fees (if any), and will remain in force until terminated.  You may terminate the licenses granted in this Agreement at any time by destroying the Software and any accompanying documentation, together with any and all copies thereof.  The licenses granted in this Agreement will terminate automatically if you breach any of its terms or conditions or any of the terms or conditions of any other agreement between you and The Company.  Upon termination, you shall immediately destroy the original and all copies of the Software and any accompanying documentation.");
        addAgreement("Distribution", "To distribute this application on any website, phone, device or other electronic medium you must first obtain a distribution licence from Crave Create");
        addAgreement("OWNERSHIP", "The Software and any documentation are owned by The Company and ownership of the Software shall at all times remain with The Company.  Copies are provided to you only to allow you to exercise your rights under this Agreement.  Without \nlimiting the generality of the foregoing, you do not receive any rights to any patents, copyrights, trade secrets, trademarks or other intellectual property rights relating to or in the Software or any accompanying documentation.  All rights not expressly granted to you under this Agreement are reserved by The Company.\n\nSubject to the terms and conditions set out in this Agreement, \"The Company\" grants you a limited, nonexclusive, nonsublicensable, and revocable right to use Software solely in accordance with the following: Termination.  The Company may terminate your license at any time, for any reason or no reason.  The Company may also terminate your license if you you shall immediately destroy all copies of the Software in your possession, custody or control. breach any of the terms and conditions set forth in this Agreement.  Upon termination, you shall immediately destroy all copies of the Software in your possession, custody or control.");
        addAgreement("Miscellaneous", "This Agreement constitutes the entire agreement between the parties concerning the Software, and may be amended only by a writing signed by both parties.   This Agreement is subject to the exclusive jurisdiction of the English courts and shall be construed in accordance with English law.  If any provision in this Agreement should be held illegal or unenforceable by a court of competent jurisdiction, such provision shall be modified to the extent necessary to render it enforceable without losing its intent, or severed from this Agreement if no such modification is possible, and other provisions of this Agreement shall remain in full force and effect.  A waiver by either party of any term or condition of this Agreement or any breach thereof, in any one instance, shall not waive such term or condition or any subsequent breach thereof.\n\nIf you agree to these terms and conditions then press \"I ACCEPT\" now.");

    }

}
